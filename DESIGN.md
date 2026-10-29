---
name: Doodle
colors:
  primary: "#49B6E5"
  secondary: "#263D5B"
  success: "#16A34A"
  warning: "#D97706"
  danger: "#DC2626"
  surface: "#FFFFFF"
  text: "#111827"
  neutral: "#FFFFFF"
typography:
  h1:
    fontFamily: "Delius Swash Caps"
    fontSize: 2.5rem
  body-md:
    fontFamily: "Delius Swash Caps"
    fontSize: 1rem
  label-caps:
    fontFamily: "JetBrains Mono"
    fontSize: 0.875rem
  sourceScale: "14/16/18/24/32/40"
  weights: "100, 200, 300, 400, 500, 600, 700, 800, 900"
rounded:
  sm: 4px
  md: 8px
spacing:
  sm: 4px
  md: 8px
  sourceScale: "4/8/12/16/24/32"
---

## Overview

Hand-drawn, sketch-like style with doodles, handwritten fonts, and imperfect lines for a playful, informal feel.

## Style Foundations

- **Visual style:** playful
- **Typography scale:** 14/16/18/24/32/40
- **Typography fonts:** primary=Delius Swash Caps, display=Delius Swash Caps, mono=JetBrains Mono
- **Typography weights:** 100, 200, 300, 400, 500, 600, 700, 800, 900
- **Color palette:** primary, secondary, neutral, success, warning, danger
- **Spacing scale:** 4/8/12/16/24/32

## Colors

- **Primary (#49B6E5):** Token from style foundations.
- **Secondary (#263D5B):** Token from style foundations.
- **Success (#16A34A):** Token from style foundations.
- **Warning (#D97706):** Token from style foundations.
- **Danger (#DC2626):** Token from style foundations.
- **Surface (#FFFFFF):** Token from style foundations.
- **Text (#111827):** Token from style foundations.
- **Neutral (#FFFFFF):** Derived from the surface token for official format compatibility.

---

## Adaptação ao app Letícia (ENEM)

O Doodle define azul como primário e raios de 4–8px. Este projeto mantém a
**linguagem** do Doodle (traço à mão, bordas tracejadas, cartões levemente
tortos, tipografia manuscrita nos títulos) com a **identidade floral** do app:

- Primary: `#E98AA3` (rosa) · Secondary: `#C9B6E4` (lavanda) · Miolo/realce: `#F3DEA6`
- Fundo: `#FBF4F2` · Cartão: `#FFFFFF` ou `#FFFCFB` · Borda: `#F1DCE1`
- Texto: `#3B2A31` · Texto fraco: `#8A7378` · Perigo: `#D96A6A`
- Raios: 14–20px nos cartões e 999px nos botões (o app é fofo; o raio pequeno do
  Doodle deixava duro no Android).
- Tipografia: título manuscrito (Delius Swash Caps ou equivalente empacotado no APK);
  corpo em Nunito, que o app já usa.
- Sombras: deslocadas e duras (`4px 4px 0` na cor do texto), nunca difusas.

Regra prática: se um elemento parecer "de banco", está errado para este app.

## Respiro (regra fixa)

Nada encosta na borda da tela. Gutter lateral de **20dp** em toda tela e em toda
lista (`contentPadding`), e cartões soltos com margem de 10–12dp quando ficam
acima da barra de navegação. Rodapé de tela é cartão com margem, não faixa colada
nas três bordas.
