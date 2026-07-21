import { readFile } from 'node:fs/promises'

const careerOsUrl = process.env.CAREEROS_API_URL ?? 'http://localhost:8080'
const ashbyUrl = 'https://jobs.ashbyhq.com'
const ashbyApiUrl = 'https://api.ashbyhq.com/posting-api/job-board'
const input = await readFile(new URL('../config/ashby-boards.txt', import.meta.url), 'utf8')
const identifiers = [...new Set(input.split(/\r?\n/).map(value => decodeURIComponent(value.trim())).filter(Boolean))]

async function request(url, options = {}) {
  const response = await fetch(url, { ...options, headers: { 'Content-Type': 'application/json', ...options.headers } })
  if (!response.ok) throw new Error(`${options.method ?? 'GET'} ${url}: ${response.status} ${await response.text()}`)
  return response.status === 204 ? undefined : response.json()
}

async function existingCompanies() {
  const companies = []
  for (let page = 0; ; page++) {
    const result = await request(`${careerOsUrl}/api/v1/companies?page=${page}&size=500&sort=name,asc`)
    companies.push(...result.content)
    if (result.last || result.content.length === 0) return companies
  }
}

function fallbackName(identifier) {
  return identifier.replace(/[._-]+/g, ' ').replace(/\b\w/g, letter => letter.toUpperCase())
}

async function board(identifier) {
  const encoded = encodeURIComponent(identifier)
  const [apiResponse, pageResponse] = await Promise.all([
    fetch(`${ashbyApiUrl}/${encoded}`),
    fetch(`${ashbyUrl}/${encoded}`),
  ])
  if (!apiResponse.ok || !pageResponse.ok) throw new Error(`board unavailable (API ${apiResponse.status}, page ${pageResponse.status})`)
  const payload = await apiResponse.json()
  if (!Array.isArray(payload.jobs)) throw new Error('Ashby response did not contain a jobs array')
  const html = await pageResponse.text()
  const title = html.match(/<title>([^<]+)<\/title>/i)?.[1]
    ?.replace(/\s+(Jobs|Careers)$/i, '').replace(/^Jobs at\s+/i, '').trim()
  const name = title && !/^(jobs|careers)$/i.test(title) ? title : fallbackName(identifier)
  return { identifier, encoded, name, jobs: payload.jobs.length }
}

const existing = await existingCompanies()
const identifiersInUse = new Set(existing.map(company => company.atsIdentifier?.toLowerCase()).filter(Boolean))
const namesInUse = new Set(existing.map(company => company.name.toLowerCase()))
const queue = identifiers.filter(identifier => !identifiersInUse.has(identifier.toLowerCase()))
const results = { requested: identifiers.length, alreadyPresent: identifiers.length - queue.length, created: [], invalid: [], duplicateName: [] }

async function worker() {
  while (queue.length) {
    const identifier = queue.shift()
    try {
      const item = await board(identifier)
      if (namesInUse.has(item.name.toLowerCase())) {
        results.duplicateName.push({ identifier, name: item.name })
        continue
      }
      const company = await request(`${careerOsUrl}/api/v1/companies`, {
        method: 'POST',
        body: JSON.stringify({
          name: item.name,
          careerUrl: `${ashbyUrl}/${item.encoded}`,
          atsType: 'ASHBY',
          priority: 'MEDIUM',
          enabled: false,
          atsIdentifier: item.identifier,
          providerType: 'ASHBY',
          fallbackProviders: [],
        }),
      })
      identifiersInUse.add(identifier.toLowerCase())
      namesInUse.add(item.name.toLowerCase())
      results.created.push({ id: company.id, name: company.name, identifier, currentJobs: item.jobs })
    } catch (error) {
      results.invalid.push({ identifier, error: error instanceof Error ? error.message : String(error) })
    }
  }
}

await Promise.all(Array.from({ length: 8 }, worker))
console.log(JSON.stringify({ ...results, createdCount: results.created.length, invalidCount: results.invalid.length }, null, 2))
