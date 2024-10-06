#!/usr/bin/env sh

# Quick and dirty script for running some of the commands to set up the project
# The last command will output the build files in the 'resources/public' directory.
#
# Requires:
# - npm
# - clojure

# for use with fontawsome-clj https://github.com/cjohansen/fontawesome-clj

clojure -Sdeps "{:deps {no.cjohansen/fontawesome-clj {:mvn/version \"2024.01.22\"} \
                        clj-http/clj-http {:mvn/version \"3.12.3\"} \
                        hickory/hickory {:mvn/version \"0.7.1\"}}}" \
  -M -m fontawesome.import :download resources 6.4.2 && echo "DONE: fontawesome icon spec downloaded to resources/font-awesome "

npm i

# Generate tailwind css
npx tailwindcss -i ./resources/public/css/main.css -o ./resources/public/css/tw.css && echo "DONE: Tailwind css generated."

# run webpack

npx webpack && echo "DONE: webpack complete"

# compile:

npm run release && echo "DONE: JS files generated in resources/public"
