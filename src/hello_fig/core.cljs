(ns hello-fig.core
  (:require [quiescent.core :as q]
            [quiescent.dom :as dom]))

(def container (.getElementById js/document "main"))

(enable-console-print!)

(def board (repeat 3 (repeat 3 {:owner :none
                                :id 1})))
(defonce app-state (atom board))

(defn update-cell [id]
  (println "update")
  (swap! app-state (fn [board]
                     (map (fn [row]
                            (map (fn [cell]
                                   (assoc cell :owner :o))
                                 row))
                            board))))


(q/defcomponent Cell [cell]
  (let [blank? (= (:owner cell) :none)]
    (dom/td {:className "cell"
             :onClick (fn []
                        (when blank?
                          (update-cell (:id cell))))}
            (when blank? "_")
            (println cell)
            (when (= (:owner cell) :o) "O"))))

(q/defcomponent Row [row]
  (dom/tr {}
          (map #(Cell %) row)))

(q/defcomponent Table [board]
  (dom/table {}
             (map #(Row %) board)))

(defn render-game [container board]
  (q/render (Table board) container))


(render-game container @app-state)


(add-watch app-state :watch
           (fn [_ _ _ new-state]
             (render-game container new-state)))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
