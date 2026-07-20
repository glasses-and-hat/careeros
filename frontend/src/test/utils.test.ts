import{describe,expect,it}from'vitest';
import{formatDate}from'@/lib/utils';

describe('formatDate',()=>{
 it('formats date-only values without shifting the calendar day',()=>{
  expect(formatDate('2026-07-18')).toBe('Jul 18');
 });

 it('formats API timestamps',()=>{
  expect(formatDate('2026-07-18T17:00:42Z')).not.toBe('—');
 });

 it('gracefully handles missing and malformed values',()=>{
  expect(formatDate()).toBe('—');
  expect(formatDate('not-a-date')).toBe('—');
 });
});
