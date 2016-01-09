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


(def winning-lines [[0 1 2] [3 4 5] [6 7 8] ;horizontal
                    [0 3 6] [1 4 7] [2 5 8] ;vertical
                    [0 4 8] [2 4 6] ;diagonals
                    ])


(defn next-player [players player]
  (first (filter #(not= % player) players)))

(defn select-cell [{board :board current-player :player} id]
  (map (fn [row]
         (map (fn [cell]
                (if (= (:id cell) id)
                  (assoc cell :owner current-player)
                  cell))
              row)) board))

(defonce app-state (atom {:board board
                          :player player}))

(defn update-cell [id]
  (swap! app-state (fn [state]
                     {:board (select-cell state id)
                      :player (next-player players (:player state))})))

(defn get-owner-from-cell-id [board id]
  (:owner (first (mapcat (fn [row]
                (filter #(= id (:id %)) row))
                board))))

(defn winner? [board winning-line]
  (when
    (and
     (apply = (map #(get-owner-from-cell-id board %) winning-line))
     (not= (get-owner-from-cell-id board (first winning-line)) :none))
   (get-owner-from-cell-id board (first winning-line))))


(defn check-for-winner [{board :board} winning-lines]
  (first
   (remove nil?
    (map #(winner? board %) winning-lines))))

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
           (Table (:board state))
           (when-let [winner (check-for-winner state winning-lines)]
             (dom/h2 {} (str "Winner: " (name winner))))))

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
