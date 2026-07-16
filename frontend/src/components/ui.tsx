import type {ButtonHTMLAttributes,HTMLAttributes,ReactNode} from 'react';import {cn} from '@/lib/utils';import {LoaderCircle,RefreshCw}from'lucide-react';
export function Button({className,...p}:ButtonHTMLAttributes<HTMLButtonElement>){return <button className={cn('btn',className)} {...p}/>};
export function Badge({className,...p}:HTMLAttributes<HTMLSpanElement>){return <span className={cn('badge',className)} {...p}/>};
export function Skeleton({className}: {className?:string}){return <div aria-hidden className={cn('animate-pulse rounded bg-zinc-200 dark:bg-zinc-800',className)}/>};
export function EmptyState({icon,title,description,action}:{icon?:ReactNode;title:string;description:string;action?:ReactNode}){return <div className="flex min-h-56 flex-col items-center justify-center gap-2 p-8 text-center">{icon}<h3 className="font-semibold">{title}</h3><p className="max-w-sm text-sm muted">{description}</p>{action}</div>};
export function ErrorState({retry}:{retry?:()=>void}){return <EmptyState icon={<RefreshCw className="text-red-500"/>} title="Something went wrong" description="CareerOS couldn't load this data. Your changes are safe." action={retry&&<Button onClick={retry}>Try again</Button>}/>};
export function PageLoader(){return <div className="grid place-items-center h-48"><LoaderCircle className="animate-spin muted" aria-label="Loading"/></div>}
