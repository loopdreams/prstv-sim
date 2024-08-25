(ns prstv-sim.styles)


;; Colours
(def colour-styles
  {"Black"  "#2b2d42"
   "Yellow" "#FFCA3A"
   "Red"    "#FF595E"
   "Blue"   "#1982C4"
   "White"  "#edf2f4"
   "Green"  "#8AC926"
   "Purple" "#6A4C93"})

#_(def colour-styles
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


(defn spinner []
  [:div
   [:div#spinner.lds-ring [:div] [:div] [:div] [:div]]])

(comment
  (get-colour-style "Black"))

(defn tooltip [msg children]
  [:div {:class "group relative flex"}
   children
   [:span {:class "absolute -top-10 scale-0 transition-all rounded bg-gray-800 p-2 text-xs text-white group-hover:scale-100"}
    msg]])



;; Tailwind

(def inactive-tab "inline-block p-4 border-b-2 border-transparent rounded-t-lg hover:text-gray-600 hover:border-gray-300 dark:hover:text-gray-300 cursor-pointer")

(def active-tab "inline-block p-4 text-blue-600 border-b-2 border-blue-600 rounded-t-lg active dark:text-blue-500 dark:border-blue-500")

(def default-h2 "font-semibold text-lg py-3 dark:text-white")

(def default-button "disabled:cursor-not-allowed disabled:opacity-25 text-gray-900 bg-white border border-gray-300 focus:outline-none hover:bg-gray-100 focus:ring-4 focus:ring-gray-100 font-medium rounded-lg text-sm px-5 py-2.5 me-2 mb-2 dark:bg-gray-800 dark:text-white dark:border-gray-600 dark:hover:bg-gray-700 dark:hover:border-gray-600 dark:focus:ring-gray-700")

(def special-button "disabled:cursor-not-allowed disabled:opacity-25 font-bold text-teal-50 bg-teal-500 border border-gray-300 focus:outline-none hover:bg-teal-400 focus:ring-4 focus:ring-gray-100 font-medium rounded-lg text-sm px-5 py-2.5 me-2 mb-2 dark:bg-gray-800 dark:text-white dark:border-gray-600 dark:hover:bg-gray-700 dark:hover:border-gray-600 dark:focus:ring-gray-700")

(def default-button-disabled "text-white bg-blue-400 dark:bg-blue-500 cursor-not-allowed font-medium rounded-lg text-sm px-5 py-2.5 text-center")

(def default-label "block mb-2 text-sm font-medium text-gray-900 dark:text-white")

(def drop-down-select "bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500")

(def default-input-field "bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500")

(def warning-label "block mb-2 text-sm font-medium text-red-700 dark:text-red-500")

(def warning-input-field "bg-red-50 border border-red-500 text-red-900 placeholder-red-700 text-sm rounded-lg focus:ring-red-500 dark:bg-gray-700 focus:border-red-500 block w-full p-2.5 dark:text-red-500 dark:placeholder-red-500 dark:border-red-500")

(def warning-text "mt-2 text-sm text-red-600 dark:text-red-500")

(def caution-text "p-4 mb-4 text-sm text-yellow-800 rounded-lg bg-yellow-50 dark:bg-gray-800 dark:text-yellow-300")

(def table-outer-div "relative overflow-x-auto shadow-md sm:rounded-lg pb-5")

(def table-el "w-full text-xs md:text-sm text-left text-gray-500 dark:text-gray-400")

(def table-caption "p-2 md:p-6 text-lg font-semibold text-left rtl:text-right text-gray-900 bg-white dark:text-white dark:bg-gray-800")

(def table-caption-p "mt-1 text-sm font-normal text-gray-500 dark:text-gray-400")

(def table-cell "px-2 md:px-6 py-1")

(def table-head "text-xs md:text-sm text-gray-700 bg-gray-50 dark:bg-gray-700 dark:text-gray-400")

(def table-body "bg-white border-b dark:bg-gray-800 dark:border-gray-700")
