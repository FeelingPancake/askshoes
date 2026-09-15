const angular = require('angular-eslint');
const prettierConflicts = require('eslint-config-prettier');
const { defineConfig } = require('eslint/config');


// @antfu/eslint-config — ESM-пакет, require() тут не сработает.
// ESLint поддерживает Promise в качестве экспортируемого конфига, поэтому
// оборачиваем в async и используем динамический import().
module.exports = (async () => {
  const { default: antfu } = await import('@antfu/eslint-config');
  const base = await antfu(
    {
      typescript: true,
      formatters: false,
      stylistic: false,
    },
    prettierConflicts,
  );

  return defineConfig([
    ...base,
    {
      files: ['**/*.ts'],
      extends: [angular.configs.tsRecommended],
      processor: angular.processInlineTemplates,
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
      extends: [angular.configs.templateRecommended, angular.configs.templateAccessibility],
      rules: {},
    },
  ]);
})();
