## Dev Server

### Configuration

You should have configured _frontend/.inveniumrc.yml_ file. If you do, skip this section. If you don't:

1. run these commands:

    ```bash
    echo "targetUrl: http://dev.internal.invenium.io/mobix/FIXME" >> .inveniumrc.yaml
    echo "cookie: 'session=FIXME'" >> .inveniumrc.yaml
    ```

2. Replace FIXME in targetUrl with an actual MobiX instance path.
3. Replace FIXME in cookie with your actual session cookie value from the actual MobiX instance.

### Running the dev server

`pnpm dev`

## Code Validation

- All possible checks: `pnpm check`
- Separate checks: `pnpm check:prettier`, `pnpm check:test` etc

### Svelte check in watch mode

`pnpm check:svelte --watch`

## Reformat and fix linting errors

`pnpm fix`

## Build FE

`pnpm build`

## Tests

- Single run: `pnpm check:test` or `pnpm test --run`
- Watch mode: `pnpm test`
- Show results in browser: `pnpm test --ui`

## Misc

Update _pnpm_ version: `corepack up`
