# setup-jdk.ps1  ★改訂版 2025‑05‑18 (r4)
# ----------------------------------------
# ◆ 目的
#   * 任意 JDK (17/21 など) と Gradle キャッシュ用フォルダーを強制指定
#   * gradle.properties を安全に更新
#   * その後 `gradlew clean build --stacktrace` を実行
# ----------------------------------------
# 使い方 (PowerShell 7 推奨)
#   PS> Set-ExecutionPolicy -Scope Process Bypass
#   PS> ./setup-jdk.ps1 -Jdk "C:\Program Files\Java\jdk-21" -GradleHome "C:\dev\gradle_home"
# ----------------------------------------
param(
    [string]$Jdk        = "C:\Program Files\Java\jdk-17",  # 17 以上 21 以下が無難
    [string]$GradleHome = "C:\dev\gradle_home"
)

function Fail([string]$msg) { Write-Error $msg ; exit 1 }

# --- 1. パス検証 & ディレクトリ作成 -------------------
if (-not (Test-Path $Jdk))        { Fail "JDK が見つかりません → $Jdk" }
if (-not (Test-Path $GradleHome)) { New-Item -Path $GradleHome -ItemType Directory -Force | Out-Null }

# --- 2. 環境変数を現在セッション & ユーザーに設定 --------
[Environment]::SetEnvironmentVariable('JAVA_HOME',        $Jdk,        'User')
[Environment]::SetEnvironmentVariable('GRADLE_USER_HOME', $GradleHome, 'User')
$env:JAVA_HOME        = $Jdk
$env:GRADLE_USER_HOME = $GradleHome

# Path の重複 JDK\bin を除去して先頭に追加 (正規表現を排除)
$jdkBin = "$Jdk\bin"
$paths  = ($env:Path -split ';') | Where-Object { $_ -and ($_ -ne $jdkBin) }
$env:Path = (($jdkBin) + ';' + ($paths -join ';')).TrimEnd(';')

Write-Host "JAVA_HOME        = $env:JAVA_HOME"        -ForegroundColor Cyan
Write-Host "GRADLE_USER_HOME = $env:GRADLE_USER_HOME" -ForegroundColor Cyan

# --- 3. gradle.properties 更新 -------------------------
$propFile = Join-Path $PSScriptRoot 'gradle.properties'
$props    = @{
    'org.gradle.java.home' = $Jdk -replace '\\','/'
    'org.gradle.user.home' = $GradleHome -replace '\\','/'
}

if (Test-Path $propFile) {
    $existing = Get-Content $propFile | Where-Object { $_ -notmatch '^org\.gradle\.(java|user)\.home=' }
    $output   = $existing + ($props.GetEnumerator() | ForEach-Object { "$($_.Key)=$($_.Value)" })
    $output | Set-Content -LiteralPath $propFile -Encoding UTF8
} else {
    $props.GetEnumerator() | ForEach-Object { "$($_.Key)=$($_.Value)" } |
        Set-Content -LiteralPath $propFile -Encoding UTF8
}

# --- 4. Gradle キャッシュ削除 --------------------------
$cacheDir = Join-Path $GradleHome 'caches'
if (Test-Path $cacheDir) {
    Write-Host "Deleting old Gradle cache …" -ForegroundColor DarkGray
    try {
        Get-ChildItem -Path $cacheDir -Recurse -Force | ForEach-Object { $_.Attributes = 'Normal' }
        Remove-Item -Path $cacheDir -Recurse -Force -ErrorAction Stop
    } catch {
        Write-Warning "キャッシュ削除中にエラー: $($_.Exception.Message)。続行します。"
    }
}

# --- 5. ビルド実行 --------------------------------------
Write-Host ("Running 'gradlew clean build' (JDK={0}, GradleHome={1})" -f $Jdk,$GradleHome) -ForegroundColor Yellow
& .\gradlew clean build --stacktrace --warning-mode all