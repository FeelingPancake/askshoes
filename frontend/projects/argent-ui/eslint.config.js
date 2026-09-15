// @ts-check
const { defineConfig } = require('eslint/config');

module.exports = (async () => {
  const rootConfig = await require('../../eslint.config.js');

  return defineConfig([
    ...rootConfig,
    {
      files: ['**/*.ts'],
      rules: {
        '@angular-eslint/directive-selector': [
          'error',
          {
            type: 'attribute',
            prefix: 'argent',
            style: 'camelCase',
          },
        ],
        '@angular-eslint/component-selector': [
          'error',
          {
            type: 'element',
            prefix: 'argent',
            style: 'kebab-case',
          },
        ],
      },
    },
    {
      files: ['**/*.html'],
      rules: {},
    },
  ]);
})();
