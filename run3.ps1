$baseFiles = @(
    "pom.xml",
    "mvnw",
    "mvnw.cmd",
    ".mvn",
    "compose.yaml",
    "src/main/resources/application.yaml",
    "src/main/java/com/portfolio/stockpricefeed/StockPriceFeedApplication.java"
)

foreach ($us in 1..9) {
    Write-Host "Processing US$us..."
    git checkout main
    git branch -D "feature/use-case-$us" 2>$null
    git checkout -b "feature/use-case-$us"
    
    foreach ($f in $baseFiles) {
        git restore --source=all-code $f
    }
    
    $files = git grep -l "US$us" all-code -- 'src/*.java'
    if ($files) {
        foreach ($file in $files) {
            $fileLines = $file -split "`n"
            foreach ($line in $fileLines) {
                if (-not [string]::IsNullOrWhiteSpace($line)) {
                    $strippedPath = $line -replace "^all-code:",""
                    git restore --source=all-code $strippedPath
                }
            }
        }
    }
    
    git add .
    $status = git status --porcelain
    if ($status) {
        git commit -m "Add files for Use Case $us"
    } else {
        Write-Host "No files added for US$us"
    }
}
git checkout all-code
