import{ArrowUpRight,Bookmark}from'lucide-react';
import{useQueryClient}from'@tanstack/react-query';
import{toast}from'sonner';
import{getListApplicationsQueryKey,useCreateApplication,useDeleteApplication}from'@/api/generated/applications/applications';
import{Button}from'@/components/ui';

type JobReference={id:string;title:string;applyUrl:string};

function useTrackJob(job:JobReference,status:'WISHLIST'|'APPLIED'){
 const client=useQueryClient();
 const remove=useDeleteApplication({mutation:{onSuccess:()=>client.invalidateQueries({queryKey:getListApplicationsQueryKey()})}});
 const create=useCreateApplication({mutation:{
  onSuccess:response=>{
   client.invalidateQueries({queryKey:getListApplicationsQueryKey()});
   const applicationId=response.data.id;
   toast.success(status==='APPLIED'?'Added to Applications':'Saved to Wishlist',{
    description:status==='APPLIED'?'Use Undo if you only opened the listing and did not submit.':undefined,
    action:applicationId?{label:'Undo',onClick:()=>remove.mutate({id:applicationId})}:undefined,
    duration:8000,
   });
  },
  onError:error=>toast.error(error instanceof Error?error.message:'Unable to track job'),
 }});
 const track=()=>create.mutate({data:{jobPostingId:job.id,status,appliedDate:status==='APPLIED'?localDate():undefined,jobLink:job.applyUrl}});
 return{track,pending:create.isPending};
}

export function ApplyToJobButton({job,className}:{job:JobReference;className?:string}){
 const action=useTrackJob(job,'APPLIED');
 const apply=()=>{window.open(job.applyUrl,'_blank','noopener,noreferrer');action.track()};
 return <Button className={className??'btn-primary'} disabled={action.pending} onClick={apply}>{action.pending?'Tracking…':'Apply'}<ArrowUpRight className="size-3"/></Button>;
}

export function SaveJobButton({job}:{job:JobReference}){
 const action=useTrackJob(job,'WISHLIST');
 return <Button disabled={action.pending} onClick={action.track} aria-label={`Save ${job.title} to Wishlist`} title="Save to Wishlist"><Bookmark className="size-3"/></Button>;
}

function localDate(){const now=new Date(),offset=now.getTimezoneOffset()*60_000;return new Date(now.getTime()-offset).toISOString().slice(0,10)}
