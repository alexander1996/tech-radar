(ns tech-radar.ui.navbar
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [tech-radar.utils.view :refer [prevent-propagation]]
            [tech-radar.state :refer [app-state]]
            [tech-radar.services.search :refer [make-search]]))

(defn brand-toggle [current-menu-item]
  (let [github-link "https://github.com/abtv/tech-radar"]
    [:div.navbar-header {}
     [:button.navbar-toggle {:type        "button"
                             :data-toggle "collapse"
                             :data-target ".navbar-ex1-collapse"}
      [:span.sr-only {} "Toggle navigation"]
      [:span.icon-bar {}]
      [:span.icon-bar {}]
      [:span.icon-bar {}]]
     [:a.navbar-brand {:href "#/"} "Tech Radar"]
     [:img {:src "images/radar.svg"}]
     [:div.topic-container
      [:span.topic-header current-menu-item]]
     (when (= current-menu-item "")
       [:div.fork-me-mobile-wrapper
        [:a.fork-me-mobile {:href   github-link
                            :target "_blank"}
         "Fork me on GitHub"]])
     (when (= current-menu-item "")
       [:a.fork-me-desktop {:href   github-link
                            :target "_blank"}
        [:img.fork-me-desktop-img {:src "images/forkme.png"}]])]))

(defn records-per-page-settings [records-per-page set-record-count]
  [:ul.nav.navbar-right.top-nav.records-per-page {}
   [:li.dropdown {}
    [:a.dropdown-toggle {:href          "#"
                         :data-toggle   "dropdown"
                         :aria-expanded "false"}
     [:i.fa.fa-gear " records per page "]
     [:b.caret]]
    [:ul.dropdown-menu
     (mapv (fn [records-count]
             [:li {:class (if (= records-per-page records-count)
                            "active"
                            nil)
                   :key   (str "records_count_" records-count)}
              [:a {:on-click #(set-record-count records-count)}
               records-count]]) [15 20 25 30])]]])

(defn menu-item [{:keys [href name selected]}]
  [:li {:key   (str "menu-item-" name)
        :class (when selected
                 "active")}
   [:a {:href href
        :alt  name}
    [:span {} name]]])

(defn sidebar-menu-items [topic-items current-topic]
  [:div.collapse.navbar-collapse.navbar-ex1-collapse {}
   [:ul.nav.navbar-nav.side-nav {}
    (mapv (fn [[id params]]
            (menu-item (assoc params :selected (= id current-topic)))) topic-items)]])

(defn search-input [navbar-component topic search-text]
  [:form.navbar-form.navbar-right.search-input {}
   [:div.input-group {}
    [:input.form-control {:type        "text"
                          :placeholder "Search..."
                          :value       (or search-text "")
                          :on-change   (fn [e]
                                         (let [value (-> e .-target .-value)]
                                           (om/transact! navbar-component `[(search-text/set {:search-text ~value})
                                                                            {:settings [:search-text]}])))}]
    [:span.input-group-btn {}
     [:button.btn.btn-default {:on-click (fn [e]
                                           (make-search app-state topic search-text)
                                           (prevent-propagation e))}
      [:i.fa.fa-search {} ""]]]]])

(defn- current-menu-item [current-screen current-topic menu-items]
  (cond
    (= :home current-screen) ""
    (= :trends current-screen) "Trends"
    :else (-> (get menu-items current-topic)
              (:name))))

(defui NavBar
  static om/IQuery
  (query [this]
    [:records-per-page
     :menu-items
     :page-number
     :search-text])
  Object
  (set-record-count [this cnt]
    (om/transact! this `[(records-per-page/set {:records-per-page ~cnt})
                         {:settings [:records-per-page]}]))
  (render [this]
    (let [{:keys [records-per-page menu-items search-text]} (om/props this)
          {:keys [current-screen current-topic]} (om/get-computed this)]
      (html [:nav.navbar.navbar-inverse.navbar-fixed-top {:role "navigation"}
             (brand-toggle (current-menu-item current-screen current-topic menu-items))
             (when (= :topic current-screen)
               (search-input this current-topic search-text))
             (when (= :topic current-screen)
               (records-per-page-settings records-per-page #(.set-record-count this %)))
             (sidebar-menu-items menu-items current-topic)]))))

(def nav-bar (om/factory NavBar))
