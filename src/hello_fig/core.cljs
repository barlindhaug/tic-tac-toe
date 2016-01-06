(ns hello-fig.core
  (:require [quiescent.core :as q]
            [quiescent.dom :as dom]))

(def container (.getElementById js/document "main"))

(enable-console-print!)

(def board (partition 3 (map (fn [id]
       {:owner :none
        :id id})
     (range 9))))

(def players [:o :x])

(def player (first players))

(defn next-player [players player]
  (println players player)
  (first (filter #(not= % player) players)))

(defonce app-state (atom {:board board
                          :player player}))

(defn update-cell [id]
  (swap! app-state (fn [state]
                     {:board (map (fn [row]
                            (map (fn [cell]
                                   (if (= (:id cell) id)
                                     (assoc cell :owner (:player state))
                                     cell))
                                 row))
                            (:board state))
                      :player (next-player players (:player state))})))


(q/defcomponent Cell [cell]
  (let [blank? (= (:owner cell) :none)]
    (dom/td {:className "cell"
             :onClick (fn []
                        (when blank?
                          (update-cell (:id cell))))}
            (when blank? "_")
            (when (= (:owner cell) :o) "O")
            (when (= (:owner cell) :x) "X"))))

(q/defcomponent Row [row]
  (dom/tr {}
          (map #(Cell %) row)))

(q/defcomponent Table [board]
  (dom/table {}
             (map #(Row %) board)))

(q/defcomponent Game [state]
  (dom/div {}
           (dom/h2 {} (str "Current player: " (name (:player state))))
           (Table (:board state))))

(defn render-game [container state]
  (q/render (Game state) container))


(render-game container @app-state)


(add-watch app-state :watch
           (fn [_ _ _ new-state]
             (render-game container new-state)))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
