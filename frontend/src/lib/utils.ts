import { clsx, type ClassValue } from 'clsx';import { twMerge } from 'tailwind-merge';export const cn=(...v:ClassValue[])=>twMerge(clsx(v));
export const formatDate=(v?:string)=>v?new Intl.DateTimeFormat(undefined,{month:'short',day:'numeric'}).format(new Date(`${v}T12:00:00`)):'—';
export const scoreTone=(n:number)=>n>=90?'bg-emerald-500/15 text-emerald-700 border-emerald-500/30 dark:text-emerald-400':n>=75?'bg-blue-500/15 text-blue-700 border-blue-500/30 dark:text-blue-400':n>=60?'bg-amber-500/15 text-amber-700 border-amber-500/30 dark:text-amber-400':'bg-zinc-500/10 text-zinc-500';
