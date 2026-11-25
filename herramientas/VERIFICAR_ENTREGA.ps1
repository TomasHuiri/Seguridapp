param([string]$SdkPath = "", [string]$BackendUrl = "http://localhost:8081/api/eventos")
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $root
if (!(Test-Path '.\app\build.gradle.kts')) { throw 'Ejecutar dentro de la raiz de Seguridapp.' }
$out = Join-Path $root 'evidencias\generadas'
New-Item -ItemType Directory -Path $out -Force | Out-Null
$summary = @('Verificacion de entrega - Seguridapp', ('Fecha: '+(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')))
foreach ($f in @('apk/Seguridapp-release.apk','app/seguridapp.jks','app/build.gradle.kts','app/proguard-rules.pro','README.md')) {
    if (Test-Path -LiteralPath $f -PathType Leaf) { $summary += "PRESENTE: $f" } else { $summary += "FALTA: $f" }
}
$apk = Join-Path $root 'apk\Seguridapp-release.apk'
if (Test-Path -LiteralPath $apk) {
    $summary += ('SHA256 APK: '+(Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash)
    if (!$SdkPath) { $SdkPath = $env:ANDROID_SDK_ROOT }
    if (!$SdkPath) { $SdkPath = $env:ANDROID_HOME }
    if (!$SdkPath -and (Test-Path 'local.properties')) {
        $line = Get-Content 'local.properties' | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
        if ($line) { $SdkPath = ($line -replace '^sdk\.dir=','').Replace('\:',':').Replace('\\','\') }
    }
    if (!$SdkPath) { $SdkPath = Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
    $signer = $null
    if (Test-Path (Join-Path $SdkPath 'build-tools')) {
        $signer = Get-ChildItem (Join-Path $SdkPath 'build-tools') -Recurse -File -Filter 'apksigner.bat' | Sort-Object FullName -Descending | Select-Object -First 1
    }
    if ($signer) {
        $signOutput = & $signer.FullName verify --verbose --print-certs $apk 2>&1
        $signCode = $LASTEXITCODE
        $signOutput | Set-Content -LiteralPath (Join-Path $out 'firma-apk.txt') -Encoding UTF8
        if ($signCode -eq 0) { $summary += 'APK: apksigner confirmo firma valida.' } else { $summary += "APK: apksigner fallo (codigo $signCode)." }
    } else { $summary += 'APK: apksigner no encontrado; usar -SdkPath con la ruta de Android SDK.' }
}
try {
    $response = Invoke-WebRequest -UseBasicParsing -Uri $BackendUrl -TimeoutSec 10
    $json = ConvertFrom-Json -InputObject $response.Content
    if (!$response.Content.TrimStart().StartsWith('[')) { throw 'El endpoint no devolvio un arreglo JSON.' }
    $summary += "Backend GET: HTTP $($response.StatusCode); $(@($json | Where-Object { $null -ne $_ }).Count) eventos."
    $summary += 'GET solamente: no demuestra persistencia, CRUD completo, ni cantidad de servicios.'
} catch { $summary += ('Backend: no se pudo confirmar GET. '+$_.Exception.Message) }
$summary += 'Pendientes manuales: instalacion de APK, flujo funcional, codigo fuente real del servidor, Trello, GitHub y capturas.'
$summary | Set-Content -LiteralPath (Join-Path $out 'verificacion-entrega.txt') -Encoding UTF8
$summary | Write-Host
