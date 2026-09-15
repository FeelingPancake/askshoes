import { definePreset } from '@primeuix/themes';

import { ArgentPreset } from 'argent-ui';

/**
 * `AskShoesPreset` — тёплый бренд AskShoes поверх нейтрального `ArgentPreset`.
 *
 * Что переопределяет относительно `ArgentPreset` (геометрия, тени, focus ring
 * наследуются без изменений — `definePreset` делает глубокое слияние):
 * - `semantic.colorScheme.light.surface` — тёплый грейж вместо нейтрального
 *   серого (12 значений);
 * - `semantic.primary` и `semantic.colorScheme.light.primary` — кирпичный
 *   акцент (`{primary.500}`) вместо «чернил» ядра. Кирпич используется только
 *   как маркер (активная вкладка, фокус, акцент в логотипе) — главная кнопка
 *   ниже остаётся чернильной;
 * - `components.button.colorScheme.light.root.primary` — главная кнопка
 *   намеренно НЕ следует за `primary` и остаётся чернильной (`{surface.900}`),
 *   как в макете: кирпич — только маркер, а не действие;
 * - `components.tag.colorScheme.light` — три статуса без «таблеток»:
 *   `warn` (кирпичный, приглушённый), `success` (оливковый), `secondary`
 *   (прозрачный фон, серая рамка). Рамка `p-tag` задаётся не токеном (в Aura
 *   его нет), а правилом `.p-tag { border: 1px solid currentColor; }` в
 *   `argent.scss` (ядро) — см. TSDoc там.
 *
 * Ключи компонентов сверены с исходниками `@primeuix/themes`:
 * `dist/aura/button/index.mjs` (root.primary: background/borderColor/
 * hover-/active- варианты/color/hoverColor/activeColor) и `dist/aura/tag/index.mjs`
 * (colorScheme.light.<severity>: background/color, без вложенного `root`).
 */
export const AskShoesPreset = definePreset(ArgentPreset, {
  semantic: {
    primary: {
      50: '#fbeee8',
      100: '#f4d6c9',
      200: '#e9b09a',
      300: '#dc8a6c',
      400: '#cc6641',
      500: '#b8471f',
      600: '#9c3b19',
      700: '#7f3015',
      800: '#632512',
      900: '#4a1c0e',
      950: '#2e1108',
    },
    colorScheme: {
      light: {
        primary: {
          color: '{primary.500}',
          hoverColor: '{primary.600}',
          activeColor: '{primary.700}',
          contrastColor: '#ffffff',
        },
        surface: {
          0: '#fbfaf7',
          50: '#f3f1ec',
          100: '#ebe7df',
          200: '#d9d4ca',
          300: '#c2bcb0',
          400: '#9b958b',
          500: '#6b655e',
          600: '#57524c',
          700: '#45413c',
          800: '#332f2b',
          900: '#1f1d1a',
          950: '#141311',
        },
      },
    },
  },
  components: {
    button: {
      colorScheme: {
        light: {
          root: {
            primary: {
              background: '{surface.900}',
              borderColor: '{surface.900}',
              hoverBackground: '{surface.800}',
              hoverBorderColor: '{surface.800}',
              activeBackground: '{surface.700}',
              activeBorderColor: '{surface.700}',
              color: '{surface.0}',
              hoverColor: '{surface.0}',
              activeColor: '{surface.0}',
            },
          },
        },
      },
    },
    tag: {
      colorScheme: {
        light: {
          warn: {
            background: '{primary.50}',
            color: '{primary.700}',
          },
          success: {
            background: '#dfe3d6',
            color: '#3c4a2c',
          },
          secondary: {
            background: 'transparent',
            color: '{surface.500}',
          },
        },
      },
    },
  },
});
