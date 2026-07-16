import { defineConfig } from 'orval'
export default defineConfig({careeros:{input:{target:'openapi/careeros.yaml'},output:{mode:'tags-split',target:'src/api/generated/careeros.ts',schemas:'src/api/generated/model',client:'react-query',httpClient:'fetch',clean:true,override:{mutator:{path:'src/api/fetcher.ts',name:'apiFetch'}}}}})
