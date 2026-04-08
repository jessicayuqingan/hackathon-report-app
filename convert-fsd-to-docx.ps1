$inputPath = 'c:\Users\Jessi\.windsurf\hackathon-report-app\docs\architecture\report-app-fsd.md'
$outputPath = 'c:\Users\Jessi\.windsurf\hackathon-report-app\docs\architecture\report-app-fsd.docx'

$word = $null
$doc = $null

try {
    $word = New-Object -ComObject Word.Application
    $word.Visible = $false
    $doc = $word.Documents.Add()

    $lines = Get-Content -LiteralPath $inputPath

    foreach ($line in $lines) {
        if ($line -match '^###\s+(.*)$') {
            $p = $doc.Paragraphs.Add()
            $p.Range.Text = $matches[1]
            $p.Range.Style = 'Heading 3'
            $p.Range.InsertParagraphAfter()
        }
        elseif ($line -match '^##\s+(.*)$') {
            $p = $doc.Paragraphs.Add()
            $p.Range.Text = $matches[1]
            $p.Range.Style = 'Heading 2'
            $p.Range.InsertParagraphAfter()
        }
        elseif ($line -match '^#\s+(.*)$') {
            $p = $doc.Paragraphs.Add()
            $p.Range.Text = $matches[1]
            $p.Range.Style = 'Heading 1'
            $p.Range.InsertParagraphAfter()
        }
        elseif ($line -match '^\-\s+(.*)$') {
            $p = $doc.Paragraphs.Add()
            $p.Range.Text = $matches[1]
            $p.Range.ListFormat.ApplyBulletDefault()
            $p.Range.InsertParagraphAfter()
        }
        elseif ($line -match '^\d+\.\s+(.*)$') {
            $p = $doc.Paragraphs.Add()
            $p.Range.Text = $matches[1]
            $p.Range.ListFormat.ApplyNumberDefault()
            $p.Range.InsertParagraphAfter()
        }
        else {
            $p = $doc.Paragraphs.Add()
            $p.Range.Text = $line
            $p.Range.InsertParagraphAfter()
        }
    }

    $format = 12
    $doc.SaveAs([ref]$outputPath, [ref]$format)
    Write-Output ("DOCX_CREATED: " + $outputPath)
}
finally {
    if ($doc -ne $null) {
        $doc.Close() | Out-Null
        [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($doc)
    }
    if ($word -ne $null) {
        $word.Quit() | Out-Null
        [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($word)
    }
}
