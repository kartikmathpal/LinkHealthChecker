# LinkHealthChecker
- Checks for broken child links in a given url
- Uses the JSOUP library to scan parent URL
- If parent URL is working fine it scans through all the a[href] in the document.
  - repeats url scanning to identify any broken links in a given web page.
