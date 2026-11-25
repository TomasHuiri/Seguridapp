param([switch]$SoloReporte)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $root
if (!(Test-Path -LiteralPath '.\gradlew.bat') -or !(Test-Path -LiteralPath '.\app\build.gradle.kts')) { throw 'Copie herramientas/ dentro de la raiz de Seguridapp.' }
$out = Join-Path $root 'evidencias\generadas'
New-Item -ItemType Directory -Path $out -Force | Out-Null
$report = Join-Path $root 'app\build\reports\coverage\test\debug\report.xml'
$reportFolder = Split-Path -Parent $report
$testFolder = Join-Path $root 'app\build\test-results\testDebugUnitTest'
$stamp = Get-Date
if (!$SoloReporte) {
    Write-Host 'Ejecutando todas las pruebas y generando cobertura de debug...'
    # Combinar stderr y stdout en CMD, antes de que PowerShell interprete stderr.
    & $env:ComSpec /d /c 'gradlew.bat :app:testDebugUnitTest :app:createDebugUnitTestCoverageReport --rerun-tasks 2>&1' | Tee-Object -FilePath (Join-Path $out 'gradle.log')
    $gradleExitCode = $LASTEXITCODE
    if ($gradleExitCode -ne 0) { throw "Gradle fallo (codigo $gradleExitCode). Revisar evidencias/generadas/gradle.log. No se certifica cobertura." }
    if (!(Test-Path -LiteralPath $report)) { throw "No se genero $report. Revise el log y el directorio de cobertura." }
    if ((Get-Item -LiteralPath $report).LastWriteTime -lt $stamp.AddSeconds(-2)) { throw 'El XML es anterior a esta ejecucion. No se usa como evidencia nueva.' }
} else { Write-Warning 'SoloReporte: se analiza el XML existente, no se garantiza que corresponda al codigo actual.' }
if (!(Test-Path -LiteralPath $report)) { throw "No existe $report" }
[xml]$doc = Get-Content -LiteralPath $report -Raw
$packages = @($doc.report.package)
$targets = @('com/example/seguridapp/repository','com/example/seguridapp/viewmodel','com/example/seguridapp/data/model')
$rows = @()
foreach ($target in $targets) {
    $matched = @($packages | Where-Object { $_.name -eq $target -or $_.name.StartsWith($target + '/') })
    if ($matched.Count -eq 0) { throw "No se encontro el paquete $target en el XML." }
    $covered = 0; $missed = 0; $bc = 0; $bm = 0
    foreach ($p in $matched) {
        $c = $p.counter | Where-Object { $_.type -eq 'LINE' }
        if (!$c) { throw "No hay contador LINE para $($p.name)." }
        $covered += [int]$c.covered; $missed += [int]$c.missed
        $b = $p.counter | Where-Object { $_.type -eq 'BRANCH' }
        if ($b) { $bc += [int]$b.covered; $bm += [int]$b.missed }
    }
    if (($covered + $missed) -eq 0) { throw "No hay lineas medibles en $target." }
    $rows += [pscustomobject]@{ Paquete = $target; Cubiertas = $covered; NoCubiertas = $missed; Total = $covered+$missed; Cobertura = [math]::Round(100*$covered/($covered+$missed),2); RamasCubiertas = $bc; RamasNoCubiertas = $bm }
}
$covered = ($rows | Measure-Object Cubiertas -Sum).Sum
$total = ($rows | Measure-Object Total -Sum).Sum
$percent = 100*$covered/$total
$global = $doc.report.counter | Where-Object { $_.type -eq 'LINE' }
$testRows = @()
if (Test-Path -LiteralPath $testFolder) {
    foreach ($f in Get-ChildItem -LiteralPath $testFolder -Filter 'TEST-*.xml' -File) {
        [xml]$t = Get-Content -LiteralPath $f.FullName -Raw
        if (!$t.testsuite) { continue }
        $s = $t.testsuite
        $testRows += [pscustomobject]@{ Suite = $s.name; Pruebas = [int]$s.tests; Fallos = [int]$s.failures; Errores = [int]$s.errors; Omitidas = [int]$s.skipped }
    }
}
$rows | Export-Csv -LiteralPath (Join-Path $out 'cobertura_por_paquete.csv') -NoTypeInformation -Encoding UTF8
$rows | Format-Table -AutoSize
$classes = @()
foreach ($p in $packages | Where-Object { $n=$_.name; @($targets | Where-Object { $n -eq $_ -or $n.StartsWith($_ + '/') }).Count -gt 0 }) {
    foreach ($cl in $p.class) {
        $c = $cl.counter | Where-Object { $_.type -eq 'LINE' }
        if ($c -and ([int]$c.missed+[int]$c.covered) -gt 0) {
            $classes += [pscustomobject]@{ Clase=$cl.name; NoCubiertas=[int]$c.missed; Cubiertas=[int]$c.covered }
        }
    }
}
$classes | Sort-Object NoCubiertas -Descending | Export-Csv -LiteralPath (Join-Path $out 'clases_por_lineas_no_cubiertas.csv') -NoTypeInformation -Encoding UTF8
$summary = @('Seguridapp - evidencia de pruebas', ('Fecha de lectura: '+(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')), ('Fecha de XML: '+(Get-Item -LiteralPath $report).LastWriteTime), ('Modo SoloReporte: '+$SoloReporte), 'Alcance: repository + viewmodel + data/model completos, sin exclusiones adicionales.', "Lineas cubiertas: $covered / $total", ('Cobertura de lineas: {0:N2}%' -f $percent))
if ($global) { $gTotal=[int]$global.covered+[int]$global.missed; if ($gTotal -gt 0) { $summary += ('Cobertura global de lineas: {0:N2}%' -f (100*[int]$global.covered/$gTotal)) } }
if ($testRows.Count -gt 0) {
    $testRows | Export-Csv -LiteralPath (Join-Path $out 'pruebas.csv') -NoTypeInformation -Encoding UTF8
    foreach ($k in @('Pruebas','Fallos','Errores','Omitidas')) { $sum = ($testRows | Measure-Object $k -Sum).Sum; $summary += "$k : $sum" }
} else { $summary += 'Sin XML de resultados de pruebas.' }
if ($percent -ge 80) { $summary += 'Umbral de 80% de lineas alcanzado en el alcance definido; verificar resultados y criterio docente.' } else { $summary += 'Umbral de 80% de lineas NO alcanzado en el alcance definido.' }
if ($SoloReporte) { $summary += 'Informe preexistente; no acredita una ejecucion nueva.' }
$summary | Set-Content -LiteralPath (Join-Path $out 'resumen.txt') -Encoding UTF8
$summary | Write-Host
Copy-Item -LiteralPath $report -Destination (Join-Path $out 'jacoco-report.xml') -Force
if (Get-Command git -ErrorAction SilentlyContinue) {
    if ((& $env:ComSpec /d /c 'git rev-parse --is-inside-work-tree 2>nul') -eq 'true') {
        & git rev-parse HEAD | Set-Content -LiteralPath (Join-Path $out 'commit.txt') -Encoding UTF8
        & git log --all '--format=%h %ad %an %s' --date=short | Set-Content -LiteralPath (Join-Path $out 'git-log.txt') -Encoding UTF8
        & git shortlog -sn --all | Set-Content -LiteralPath (Join-Path $out 'git-autores.txt') -Encoding UTF8
        & git status --short | Set-Content -LiteralPath (Join-Path $out 'git-estado.txt') -Encoding UTF8
    } else { Write-Warning 'ZIP sin .git: ejecutar desde el repositorio clonado para acreditar autoria.' }
}
Write-Host "Archivos guardados en $out"
Write-Host "Informe HTML: $reportFolder\index.html"
