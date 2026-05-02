$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$frontendDir = Join-Path $root "frontend"

if (-not (Test-Path $frontendDir)) {
    throw "Не найдена папка frontend: $frontendDir"
}

Write-Host "Запускаю backend (gradlew.bat bootRun)..."
$backend = Start-Process -FilePath (Join-Path $root "gradlew.bat") `
    -ArgumentList "bootRun" `
    -WorkingDirectory $root `
    -PassThru

Start-Sleep -Seconds 2

if ($backend.HasExited) {
    throw "Backend завершился сразу после старта (код: $($backend.ExitCode))."
}

Write-Host "Запускаю frontend (npm run dev)..."
$frontend = Start-Process -FilePath "npm.cmd" `
    -ArgumentList "run", "dev" `
    -WorkingDirectory $frontendDir `
    -PassThru

try {
    Write-Host "Оба сервиса запущены. Жду завершения backend..."
    Wait-Process -Id $backend.Id
}
finally {
    Write-Host "Backend остановлен. Останавливаю frontend и освобождаю порт..."
    if ($frontend -and -not $frontend.HasExited) {
        Stop-Process -Id $frontend.Id -Force
    }
}

