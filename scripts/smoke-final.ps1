[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$pass = "Abc123456"
$results = @()

function Login($u) {
  $r = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body ('{"username":"' + $u + '","password":"' + $pass + '"}') -UseBasicParsing -TimeoutSec 10
  $j = $r.Content | ConvertFrom-Json
  return $j.data.accessToken
}

function Get2($name, $uri, $h) {
  try {
    $r = Invoke-WebRequest -Uri $uri -Headers $h -UseBasicParsing -TimeoutSec 15
    $script:results += "PASS  $($r.StatusCode)  $name"
  } catch {
    $code = "?"
    if ($_.Exception.Response) { $code = [int]$_.Exception.Response.StatusCode }
    $script:results += "FAIL  $code  $name"
  }
}

# ===== Student =====
$st = Login "ftetest"
$sh = @{Authorization = "Bearer $st"}
Get2 "ST user/me" "http://localhost:8080/api/user/me" $sh
Get2 "ST points/me" "http://localhost:8080/api/points/me" $sh
Get2 "ST jobs list" "http://localhost:8080/api/jobs?page=1&size=5" $sh
Get2 "ST job detail" "http://localhost:8080/api/jobs/1" $sh
Get2 "ST my applied" "http://localhost:8080/api/jobs/me/applied?page=1&size=5" $sh
Get2 "ST my favorites" "http://localhost:8080/api/jobs/me/favorites?page=1&size=5" $sh
Get2 "ST favorite ids" "http://localhost:8080/api/jobs/me/favorite-ids" $sh
Get2 "ST job stats" "http://localhost:8080/api/jobs/stats/overview" $sh
Get2 "ST companies" "http://localhost:8080/api/companies?page=1&size=5" $sh
Get2 "ST company detail" "http://localhost:8080/api/companies/1" $sh
Get2 "ST portfolio list" "http://localhost:8080/api/portfolio?page=1&size=5" $sh
Get2 "ST my portfolio" "http://localhost:8080/api/portfolio/me?page=1&size=5" $sh
Get2 "ST questions" "http://localhost:8080/api/interview/questions?page=1&size=5" $sh
Get2 "ST simulator scenarios" "http://localhost:8080/api/simulator/scenarios" $sh
Get2 "ST simulator sessions" "http://localhost:8080/api/simulator/sessions/me?page=1&size=5" $sh
Get2 "ST badge templates" "http://localhost:8080/api/badges/templates" $sh
Get2 "ST my badges" "http://localhost:8080/api/user/badges" $sh
Get2 "ST whitepaper" "http://localhost:8080/api/salary/whitepaper/latest" $sh
Get2 "ST my salary contributions" "http://localhost:8080/api/salary/contributions/me?page=1&size=5" $sh
Get2 "ST notifications" "http://localhost:8080/api/notify/me?page=1&size=5" $sh
Get2 "ST unread count" "http://localhost:8080/api/notify/me/unread-count" $sh
Get2 "ST my resumes" "http://localhost:8080/api/resume/me" $sh
Get2 "ST interview history" "http://localhost:8080/api/interview/history?page=1&size=5" $sh

# ===== HR =====
$hr = Login "demo_hr"
$hh = @{Authorization = "Bearer $hr"}
Get2 "HR user/me" "http://localhost:8080/api/user/me" $hh
Get2 "HR my published jobs" "http://localhost:8080/api/jobs/me/published?page=1&size=10" $hh

# ===== Admin =====
$ad = Login "admin"
$ah = @{Authorization = "Bearer $ad"}
Get2 "AD overview" "http://localhost:8080/api/admin/overview" $ah
Get2 "AD salary pending" "http://localhost:8080/api/admin/salary/pending?page=1&size=10" $ah
Get2 "AD badge templates" "http://localhost:8080/api/admin/badges/templates" $ah

# ===== Authz: student -> admin API should be 403 =====
try {
  $r = Invoke-WebRequest -Uri "http://localhost:8080/api/admin/overview" -Headers $sh -UseBasicParsing -TimeoutSec 10
  $results += "WARN  200  AUTHZ student->admin (expect 403)"
} catch {
  $code = [int]$_.Exception.Response.StatusCode
  if ($code -eq 403) { $results += "PASS  403  AUTHZ student->admin blocked" } else { $results += "WARN  $code  AUTHZ student->admin unexpected" }
}

# ===== No token should be 401 =====
try {
  $r = Invoke-WebRequest -Uri "http://localhost:8080/api/user/me" -UseBasicParsing -TimeoutSec 10
  $results += "WARN  200  NOTOKEN (expect 401)"
} catch {
  $code = [int]$_.Exception.Response.StatusCode
  if ($code -eq 401) { $results += "PASS  401  NOTOKEN blocked" } else { $results += "WARN  $code  NOTOKEN unexpected" }
}

$results
$fail = ($results | Where-Object { $_ -like "FAIL*" }).Count
$passc = ($results | Where-Object { $_ -like "PASS*" }).Count
""
"TOTAL: PASS=$passc FAIL=$fail WARN=$(($results | Where-Object { $_ -like 'WARN*' }).Count)"
