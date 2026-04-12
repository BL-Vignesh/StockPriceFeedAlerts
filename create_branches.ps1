git checkout -b all-code
git add .
git commit -m "Backup all code"

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
    
    # Delete the branch if it already exists to recreate a fresh one from main
    git branch -D "feature/use-case-$us" 2>$null
    
    git checkout -b "feature/use-case-$us"
    
    # Restore base files
    foreach ($f in $baseFiles) {
        git restore --source=all-code $f
    }
    
    # Find all specific java files for this use case
    $files = git grep -l "US$us" all-code -- 'src/*.java'
    if ($files) {
        foreach ($file in $files) {
            # split is needed because git grep might return multi-line string combined
            $fileLines = $file -split "`n"
            foreach ($line in $fileLines) {
                if (-not [string]::IsNullOrWhiteSpace($line)) {
                    git restore --source=all-code $line
                }
            }
        }
    }
    
    git add .
    $status = git status --porcelain
    if ($status) {
        git commit -m "Add files for Use Case $us"
    } else {
        Write-Host "Nothing added for Use Case $us"
    }
}

# Finally go back to the all-code branch so the workspace contains all files
git checkout all-code
