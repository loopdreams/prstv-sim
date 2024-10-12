# Proportional Representation Single Transferable Vote Simulator

A Clojure webapp building using [re-frame](https://github.com/day8/re-frame).

Simulate ballots and results for an election that uses the [single transferable vote](https://en.wikipedia.org/wiki/Single_transferable_vote) method of counting.

You can view a live version of the site [here](https://eoin.site/prstv/).

You can view a write-up of some of the code behind the ballot generation/counting [here](https://eoin.site/prstv-docs/).

# Building

To build the app you can have a look at the `build.sh` file for some of the commands to run. 

Broadly, the app depends on clojure and npm.

Building involves the following steps:

1. Install the specs for font-awesome icons that are loaded with [fontawesome-clj](https://github.com/cjohansen/fontawesome-clj)

``` sh
clojure -Sdeps "{:deps {no.cjohansen/fontawesome-clj {:mvn/version \"2024.01.22\"} \
                        clj-http/clj-http {:mvn/version \"3.12.3\"} \
                        hickory/hickory {:mvn/version \"0.7.1\"}}}" \
  -M -m fontawesome.import :download resources 6.4.2 && echo "DONE: fontawesome icon spec downloaded to resources/font-awesome "
```

2. Install the npm modules

``` sh
npm i
```

3. Compile the tailwind css

``` sh
npx tailwindcss -i ./resources/public/css/main.css -o ./resources/public/css/tw.css 
```

4. Compile the bundled javascript

``` sh
npx webpack
```

5. Complie the clojurescript files to javascript

``` sh
npm run release
```

Then, all the static files needed for the app should be in `resources/public`
