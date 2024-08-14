(ns prstv-sim.styles)


;; Colours
(def colour-styles
  {"Black"  "#161925"
   "Yellow" "#F1D302"
   "Red"    "#C1292E"
   "Blue"   "#235789"
   "White"  "#FDFFFC"
   "Green"  "#8A9546"
   "Purple" "#72405C"})

(def party-colours-list (keys colour-styles))

(defn get-colour-style [key]
  (let [data {:style {:background-color ""}}
        data (if (or (= key "Yellow") (= key "White") (= key "Green"))
               (assoc-in data [:style :color] (colour-styles "Black"))
               (assoc-in data [:style :color] (colour-styles "White")))]
    (assoc-in data [:style :background-color] (colour-styles key))))

(comment
  (get-colour-style "Black"))

;; Tailwind
(def default-h2 "font-semibold text-lg py-3")

(def default-button "text-gray-900 bg-white border border-gray-300 focus:outline-none hover:bg-gray-100 focus:ring-4 focus:ring-gray-100 font-medium rounded-lg text-sm px-5 py-2.5 me-2 mb-2 dark:bg-gray-800 dark:text-white dark:border-gray-600 dark:hover:bg-gray-700 dark:hover:border-gray-600 dark:focus:ring-gray-700")

(def default-button-disabled "text-white bg-blue-400 dark:bg-blue-500 cursor-not-allowed font-medium rounded-lg text-sm px-5 py-2.5 text-center")

(def default-label "block mb-2 text-sm font-medium text-gray-900 dark:text-white")

(def drop-down-select "bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500")

(def default-input-field "bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500")

(def table-outer-div "relative overflow-x-auto shadow-md sm:rounded-lg")

(def table-el "w-full text-sm text-left rtl:text-right text-gray-500 dark:text-gray-400")

(def table-caption "p-5 text-lg font-semibold text-left rtl:text-right text-gray-900 bg-white dark:text-white dark:bg-gray-800")

(def table-caption-p "mt-1 text-sm font-normal text-gray-500 dark:text-gray-400")

(def table-head "text-s text-gray-700 bg-gray-50 dark:bg-gray-700 dark:text-gray-400")

(def table-body "bg-white border-b dark:bg-gray-800 dark:border-gray-700")
