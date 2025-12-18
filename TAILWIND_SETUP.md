# Tailwind CSS Integration Guide (Spring Boot + Thymeleaf)

This guide explains how Tailwind CSS is set up in this project and how to use it effectively with Thymeleaf templates.

## Prerequisites
- Node.js and npm installed (macOS: `brew install node`) 
- Java 21 / Spring Boot app builds and serves static resources from `src/main/resources/static`

## Install Dev Dependencies
```bash
npm install -D tailwindcss postcss autoprefixer
```

## Initialize Tailwind + PostCSS
This creates `tailwind.config.js` and `postcss.config.js`.
```bash
npx tailwindcss init -p
```

## Configure Tailwind
Edit `tailwind.config.js` to scan Thymeleaf templates and define project colors.
```js
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/main/resources/templates/**/*.html"
  ],
  theme: {
    extend: {
      colors: {
        primary: '#667eea',
        secondary: '#764ba2',
      },
    },
  },
  plugins: [],
}
```

## Create CSS Entry and Build Output
Create the input file with Tailwind directives:
- `src/main/resources/static/css/input.css`
```css
@tailwind base;
@tailwind components;
@tailwind utilities;
```

Build the CSS output (minified for production):
```bash
npx tailwindcss -i ./src/main/resources/static/css/input.css -o ./src/main/resources/static/css/output.css --minify
```

Alternatively, use the npm script (watch mode already exists):
```bash
npm run build:css
```

## Reference CSS in Thymeleaf Templates
Replace any Tailwind CDN `<script>` with a stylesheet link to the built CSS:
```html
<link rel="stylesheet" th:href="@{/css/output.css}">
```
Add the link inside the `<head>` of each template. Examples:
- `src/main/resources/templates/index.html`
- `src/main/resources/templates/auth/login.html`
- `src/main/resources/templates/auth/register.html`
- `src/main/resources/templates/dashboard/admin-dashboard.html`
- `src/main/resources/templates/dashboard/company-dashboard.html`
- `src/main/resources/templates/dashboard/user-dashboard.html`

## Development Workflow
- Start Tailwind in watch mode to auto-rebuild on changes:
```bash
npm run build:css
```
- Edit templates and classes; Tailwind will rebuild `output.css` when you save.

## Production Build
- Ensure `output.css` is up to date (use `--minify`).
- Build the Spring Boot app:
```bash
./gradlew clean build -x test
```
- Run the JAR:
```bash
java -jar build/libs/*.jar
```

## Purge/Content Paths
Tailwind scans only Thymeleaf HTML files:
```js
content: ["./src/main/resources/templates/**/*.html"]
```
If you add JS components that contain Tailwind classes, include those paths too.

## Troubleshooting
- Browserslist warning:
```bash
npx update-browserslist-db@latest
```
- CSS not loading:
  - Verify the link tag uses `th:href="@{/css/output.css}"`.
  - Confirm the file exists at `src/main/resources/static/css/output.css`.
  - Check Spring Boot serves static resources: `/css/output.css` should return 200.

## Optional: Add Production Script
Add an npm script for a one-off minified build:
```json
{
  "scripts": {
    "build:css:prod": "tailwindcss -i ./src/main/resources/static/css/input.css -o ./src/main/resources/static/css/output.css --minify"
  }
}
```

## Removing Tailwind CDN Scripts
We removed CDN `<script>` tags from templates and moved to a local stylesheet. This ensures consistent builds, better performance, and no runtime config drift.
