import { readFile } from 'node:fs/promises'

const baseUrl = process.env.CAREEROS_API_URL ?? 'http://localhost:8080'
const csv = await readFile(new URL('../config/company-seed.csv', import.meta.url), 'utf8')
const rows = csv.trim().split(/\r?\n/).slice(1).map(line => {
  const [name, ats, identifier, tier, categories, providerHost] = line.split(',')
  return { name, ats, identifier, tier: Number(tier), categories: categories.split(';'), providerHost }
})

async function request(path, options = {}) {
  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers },
  })
  if (!response.ok) throw new Error(`${options.method ?? 'GET'} ${path}: ${response.status} ${await response.text()}`)
  return response.status === 204 ? undefined : response.json()
}

const companyPage = await request('/api/v1/companies?size=500')
const companies = new Map(companyPage.content.map(company => [company.name.toLowerCase(), company]))

function careerUrl(row) {
  if (row.ats === 'GREENHOUSE') return `https://boards.greenhouse.io/${row.identifier}`
  if (row.ats === 'ASHBY') return `https://jobs.ashbyhq.com/${row.identifier}`
  if (row.ats === 'LEVER') return `https://jobs.lever.co/${row.identifier}`
  if (row.ats === 'WORKDAY' && row.providerHost) {
    const [, site] = row.identifier.split('/', 2)
    return `https://${row.providerHost}/${site}`
  }
  return `https://www.google.com/search?q=${encodeURIComponent(`${row.name} careers`)}`
}

let created = 0
for (const row of rows) {
  if (companies.has(row.name.toLowerCase())) continue
  const company = await request('/api/v1/companies', {
    method: 'POST',
    body: JSON.stringify({
      name: row.name,
      careerUrl: careerUrl(row),
      atsType: row.ats,
      priority: row.tier >= 9 ? 'HIGH' : 'MEDIUM',
      enabled: row.ats !== 'OTHER',
      atsIdentifier: row.identifier || undefined,
      providerType: row.ats !== 'OTHER' ? row.ats : undefined,
      providerConfiguration: row.providerHost ? JSON.stringify({ host: row.providerHost }) : undefined,
      fallbackProviders: [],
    }),
  })
  companies.set(row.name.toLowerCase(), company)
  created++
}

const watchlistPage = await request('/api/watchlists?size=100')
const watchlists = new Map(watchlistPage.content.map(watchlist => [watchlist.name, watchlist]))
const categoryNames = [...new Set(rows.flatMap(row => row.categories))]
let memberships = 0

for (const category of categoryNames) {
  let watchlist = watchlists.get(category)
  if (!watchlist) {
    watchlist = await request('/api/watchlists', { method: 'POST', body: JSON.stringify({ name: category }) })
    watchlists.set(category, watchlist)
  }
  const existing = new Set(watchlist.companies.map(company => company.id))
  for (const row of rows.filter(item => item.categories.includes(category))) {
    const company = companies.get(row.name.toLowerCase())
    if (existing.has(company.id)) continue
    await request(`/api/watchlists/${watchlist.id}/companies/${company.id}`, {
      method: 'PUT',
      body: JSON.stringify({ priority: row.tier === 10 ? 100 : row.tier === 9 ? 90 : 75, enabled: true }),
    })
    memberships++
  }
}

console.log(JSON.stringify({ catalogCompanies: rows.length, createdCompanies: created, watchlists: categoryNames.length, createdMemberships: memberships }, null, 2))
