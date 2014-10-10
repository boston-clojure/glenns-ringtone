(ns glenns-ringtone.core
    (:use [overtone.live]
          [overtone.inst.piano]))

;; play glenn's ringtone without the swing and the flair.
;; that's all!

(def state
  (atom {:measure-number 0
         :is-running true}))
(let [
      ;; milliseconds for each tick
      increment 100
      music-list (map-indexed
                  (fn [index pitch]
                    {:onset (* index increment)
                     :data [{:duration increment
                             :pitch pitch}]}
                    )
                  ;; list of pitches, transpose by 2 octaves
                  (map #(+ 24 %) [68 69 72
                                  68 69 72
                                  68 69 72
                                  68 69 72
                                  65 66 62]))

      start-time (now)
      ;; not useful now... to find the total duration of the entire jingle
      ;; max-duration (+ (apply max (map :onset music-list))
      ;;                 (apply max (map :duration (last (map :data music-list)))))

      ;; call this on every timer tick (per increment)
      playfunction (fn []
                     (if (< (@state :measure-number) (count music-list))
                       (let [current-measure (nth music-list (@state :measure-number))
                             current-time (now)]
                         (when (< (+ start-time (:onset current-measure)) current-time)
                           ;;(piano (:pitch (first (:data current-measure))))
                           (doseq [pitch (map :pitch (:data current-measure))]
                             (piano pitch))
                           (swap! state assoc :measure-number (inc (@state :measure-number)))

                           )
                         )
                       (swap! state assoc :is-running false)
                       )
                     )
      tasktimer (java.util.Timer.)]
  (.scheduleAtFixedRate tasktimer
                        (proxy [java.util.TimerTask] []
                          (run []
                            (if (@state :is-running)
                              (do
                                (.start
                                 (Thread. playfunction)))
                              (do
                                (.cancel tasktimer)))))
                        0 ;; first run delay
                        increment ;; thereafter
                        ))
