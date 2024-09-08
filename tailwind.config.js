/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,js,cljs}"],
  theme: {
    extend: {
      fontFamily: {
        'noto-sans': ['"Noto Sans"', "sans"],
        'raleway' : ['"Raleway"']
      },
    },
  },
  plugins: [
  require('@tailwindcss/typography')
  ]
};
