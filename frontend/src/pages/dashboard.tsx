import { ArrowUpRight, BriefcaseBusiness, CalendarCheck, Clock3, Sparkles, Building2, MapPin, Bookmark, type LucideIcon } from 'lucide-react'
import { useGetDashboard, useGetReminders } from '@/api/generated/discovery/discovery'
import { useListResumes } from '@/api/generated/resumes/resumes'
import type { JobSummary } from '@/api/generated/model'
import { Page } from '@/components/page'
import { Badge, Button, ErrorState, Skeleton } from '@/components/ui'
import { formatDate, scoreTone } from '@/lib/utils'

export default function DashboardPage() {
  const query = useGetDashboard()
  const reminders = useGetReminders()
  const resumes = useListResumes({ size: 5 })
  const dashboard = query.data?.data
  if (query.isError) return <Page title="Dashboard"><ErrorState retry={() => query.refetch()} /></Page>
  const hour = new Date().getHours()
  const greeting = hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening'
  const stats: [string, number | undefined, LucideIcon][] = [
    ['New jobs', dashboard?.newJobsToday, Sparkles],
    ['High matches', dashboard?.highMatchJobs, BriefcaseBusiness],
    ['In progress', Number(dashboard?.statistics?.applicationsThisMonth ?? 0), Clock3],
    ['Interviews', Math.round(Number(dashboard?.statistics?.interviewRate ?? 0)), CalendarCheck],
    ['Follow-ups', dashboard?.applicationsNeedingFollowUp, Clock3],
  ]
  return <Page title={`${greeting}, Rahul`} description={new Intl.DateTimeFormat(undefined, { weekday: 'long', month: 'long', day: 'numeric' }).format(new Date())}>
    <div className="grid grid-cols-2 border-y md:grid-cols-5">{stats.map(([label, value, Icon]) => <div key={label} className="border-r px-4 py-3 last:border-r-0"><div className="flex items-center gap-2 text-xs muted"><Icon className="size-3.5" />{label}</div>{query.isLoading ? <Skeleton className="mt-2 h-6 w-12" /> : <p className="mt-1 text-2xl font-semibold tabular-nums">{value ?? 0}</p>}</div>)}</div>
    <section className="mt-6"><div className="mb-3 flex items-center justify-between"><div><h2 className="font-semibold">Top matches</h2><p className="text-xs muted">Best opportunities based on your preferences</p></div><Button onClick={() => location.assign('/jobs')}>View all <ArrowUpRight className="size-3" /></Button></div>{query.isLoading ? <div className="grid gap-3 lg:grid-cols-2">{[1, 2, 3, 4].map(x => <Skeleton key={x} className="h-40" />)}</div> : <div className="grid gap-3 lg:grid-cols-2">{dashboard?.topMatches?.slice(0, 4).map(job => <MatchCard key={job.id} job={job} />)}</div>}</section>
    <div className="mt-6 grid gap-6 lg:grid-cols-[1fr_360px]"><section><h2 className="mb-3 font-semibold">Recent activity</h2><div className="panel divide-y">{dashboard?.recentJobs?.slice(0, 6).map(job => <div key={job.id} className="flex items-center gap-3 p-3"><div className="grid size-8 place-items-center rounded-md bg-zinc-100 dark:bg-zinc-800"><Building2 className="size-4" /></div><div className="min-w-0 flex-1"><p className="truncate text-sm"><b>{job.company}</b> posted <b>{job.title}</b></p><p className="text-xs muted">{formatDate(job.postedDate)}</p></div><Badge className={scoreTone(job.overallScore)}>{job.overallScore}%</Badge></div>)}</div></section><section><h2 className="mb-3 font-semibold">Upcoming reminders</h2><div className="panel divide-y">{reminders.data?.data?.slice(0, 5).map((r, i) => <div key={i} className="p-3"><div className="flex gap-2"><Clock3 className="mt-0.5 size-4 text-amber-500" /><div><p className="text-sm font-medium">{r.message}</p><p className="text-xs muted">{formatDate(r.dueDate)}</p></div></div></div>)}{!reminders.isLoading && !reminders.data?.data?.length && <p className="p-6 text-center text-sm muted">No reminders due.</p>}</div></section></div>
    <section className="mt-6"><div className="mb-3 flex items-center justify-between"><div><h2 className="font-semibold">Recent resume generations</h2><p className="text-xs muted">Local, grounded versions generated for your jobs</p></div><Button onClick={() => location.assign('/resumes')}>Resume Library <ArrowUpRight className="size-3" /></Button></div><div className="panel divide-y">{resumes.data?.data.content?.map(v=><div className="flex items-center gap-3 p-3 text-sm" key={v.id}><b>v{v.versionNumber}</b><span className="flex-1 truncate">{v.jobTitle}</span><Badge>{v.validationResult??v.status}</Badge><span className="text-xs muted">{v.generationDurationMs?`${(v.generationDurationMs/1000).toFixed(1)}s`:'—'}</span></div>)}{!resumes.isLoading&&!resumes.data?.data.content?.length&&<p className="p-6 text-center text-sm muted">No resumes generated yet.</p>}</div></section>
  </Page>
}

function MatchCard({ job }: { job: JobSummary }) {
  return <article className="panel group p-4 transition-shadow hover:shadow-md"><div className="flex items-start gap-3"><div className="grid size-10 shrink-0 place-items-center rounded-lg bg-zinc-100 font-semibold dark:bg-zinc-800">{job.company[0]}</div><div className="min-w-0 flex-1"><p className="text-xs muted">{job.company}</p><h3 className="truncate font-semibold">{job.title}</h3><div className="mt-2 flex flex-wrap gap-1.5"><Badge className={scoreTone(job.overallScore)}>{job.overallScore}% match</Badge>{job.remote && <Badge>Remote</Badge>}<span className="inline-flex items-center gap-1 text-xs muted"><MapPin className="size-3" />{job.location || 'Flexible'}</span></div></div></div><div className="mt-4 flex items-center justify-between border-t pt-3"><span className="text-xs muted">Posted {formatDate(job.postedDate)}</span><div className="flex gap-2"><Button aria-label={`Save ${job.title}`}><Bookmark className="size-3" /></Button><Button className="btn-primary">Apply <ArrowUpRight className="size-3" /></Button></div></div></article>
}
