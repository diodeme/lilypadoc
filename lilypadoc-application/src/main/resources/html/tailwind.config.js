/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        './template/js/**/*.js',
        './plugin-resource/**/*.{html,js}',
        './**/*.html'
    ],
    theme: {
        extend: {
            fontFamily: {
                mono: ['Source Han Mono', 'Sarasa Gothic', 'monospace']
            },
            border: {
                '0.1': '0.01rem', // 1px (默认情况下是16px等于1rem)
            }
        }
    },
    plugins: [require('@tailwindcss/typography')],
    daisyui: {
        themes: [
            "light",
            "dark",
            "cupcake",
            "bumblebee",
            "emerald",
            "corporate",
            "synthwave",
            "retro",
            "cyberpunk",
            "valentine",
            "halloween",
            "garden",
            "forest",
            "aqua",
            "lofi",
            "pastel",
            "fantasy",
            "wireframe",
            "black",
            "luxury",
            "dracula",
            "cmyk",
            "autumn",
            "business",
            "acid",
            "lemonade",
            "night",
            "coffee",
            "winter",
            "dim",
            "nord",
            "sunset",
        ],
    },
};