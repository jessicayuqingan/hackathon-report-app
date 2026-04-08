# Data Analysis

## Core Analysis Principles

- Validation First: Always check for missing values (NaN), outliers, and data types before performing calculations.
- Vectorization: Prefer pandas and numpy vectorized operations over manual for loops for performance.
- Reproducibility: Every analysis script must include a "Data Loading" section and a "Summary Statistics" output.

## Technical Stack & Libraries

When performing analysis, default to these tools:

- Primary: pandas, numpy, matplotlib, seaborn.
- Statistics: scipy.stats, statsmodels.
- Interactive: Generate code compatible with `.ipynb` (Jupyter Notebooks) when requested.

## Data Handling Standards

- Loading: Always use `encoding='utf-8'` and detect delimiters automatically if not CSV.
- Cleaning:
  - Standardize column names to `snake_case`.
  - Convert date columns to `datetime64[ns]` immediately.
- Visualization:
  - Use seaborn for statistical plots.
  - All charts must have a title, labeled axes, and a legend if multiple series exist.
  - Default style: `sns.set_theme(style="whitegrid")`.

## Automated Reporting Workflow

When I ask to "Analyze [file]", Cascade should:

- Peek: Run `df.info()` and `df.head()` to understand structure.
- Describe: Provide `df.describe()` for numerical columns.
- Visualize: Plot the distribution of the target variable.
- Export: Save cleaned data to a `./processed/` folder.
