(ns datomic-demo.core
  (:require [datomic.api :as d]
            [clojure.java.io :as io]))

;; (d/create-database "datomic:mem://listedb")
;; (def conn (d/connect "datomic:mem://listedb"))

;; (def schema
;;   [{:db/id                 #db/id[:db.part/db]
;;     :db/ident              :list/id
;;     :db/valueType          :db.type/string
;;     :db/cardinality        :db.cardinality/one
;;     :db/unique             :db.unique/identity
;;     :db.install/_attribute :db.part/db}

;;    {:db/id                 #db/id[:db.part/db]
;;     :db/ident              :list/type
;;     :db/valueType          :db.type/keyword                 ;; numberd, checked, bullet
;;     :db/cardinality        :db.cardinality/one
;;     :db.install/_attribute :db.part/db}

;;    {:db/id                 #db/id[:db.part/db]
;;     :db/ident              :list/items
;;     :db/valueType          :db.type/ref
;;     :db/cardinality        :db.cardinality/many
;;     :db/isComponent        true
;;     :db.install/_attribute :db.part/db}

;;    ;; ITEMS

;;    {:db/id                 #db/id[:db.part/db]
;;     :db/ident              :item/id
;;     :db/valueType          :db.type/uuid
;;     :db/cardinality        :db.cardinality/one
;;     :db/unique             :db.unique/identity
;;     :db.install/_attribute :db.part/db}

;;    {:db/id                 #db/id[:db.part/db]
;;     :db/ident              :item/content
;;     :db/valueType          :db.type/string
;;     :db/cardinality        :db.cardinality/one
;;     :db.install/_attribute :db.part/db}

;;    {:db/id                 #db/id[:db.part/db]
;;     :db/ident              :item/checked
;;     :db/valueType          :db.type/boolean
;;     :db/cardinality        :db.cardinality/one
;;     :db.install/_attribute :db.part/db}])

(defn bootstrap-db
  []
  (println "creating database")
  (d/create-database "datomic:mem://listdb")
  (println "connecting to database...")
  (let [conn (d/connect "datomic:mem://listdb")]

    (println "Installing database schemas")
    @(d/transact conn (read-string (slurp (io/resource "schema.edn"))))

    (println "installing boostrap data")
    @(d/transact conn (read-string (slurp (io/resource "bootstrap.edn"))))

    conn))

(def CONN (bootstrap-db))

#_(d/q '[:find [(pull ?e [*]) ...]
       :where
       [?e :list/id ?list-id]
       [?e :list/items ?item]
       [?item :item/checked false]]
     (d/db CONN))

;; @(d/transact CONN [[:db/add 17592186045422 :item/content "Content 4 Version 3"]])

(defn find-all-items [db]
  (d/q '[:find [(pull ?e [:db/id :item/id :item/content]) ...]
         :where
         [?e :item/id ?id]]
       db))

(defn get-history-for-entity [db eid attr]
  (d/q '[:find ?tx ?date ?v
         :in $ ?eid ?attr
         :where
         [?eid ?attr ?v ?tx true]
         [?tx :db/txInstant ?date]]
       (d/history db)
       eid
       attr))

(->> (get-history-for-entity (d/db CONN) 17592186045422 :item/content)
     (sort-by first))

(def db-1 (d/db CONN ))

(d/pull db-1 '[*] 17592186045422)

@(d/transact CONN [[:db/add 17592186045422 :item/content "Content 4 Version 4"]])

(d/t->tx (d/basis-t db-1))

(def db-2 (d/db CONN))

(d/pull db-2 '[*] 17592186045422)

(comment)(find-all-items (d/as-of (d/db CONN) "transact id eller timestamp"))

(comment
 @(d/transact conn schema)

 (defn add-list
   [conn tx-map]
   @(d/transact conn [tx-map]))

 (doseq [i (range 20)]
   (add-list conn {:db/id (d/tempid :db.part/user)
                   :list/id (str i)
                   :list/type :checked
                   }))

 (d/q '[:find ?e ?id
        :where
        [?e :list/id ?id]
        [?e :list/items ?items]]
      (d/db conn))

 (defn add-item
   [conn list-eid item]
   #_@(d/transact conn [[:db/add list-eid :list/items items]])
   @(d/transact conn [{:db/id list-eid
                       :list/items [item]}]))

 (defn retract-item [conn item-eid]
   @(d/transact conn [[:db.fn/retractEntity item-eid]]))

 (def temp-id (d/tempid :db.part/user))

 (add-item conn [:list/id "5"] {:db/id temp-id
                                :item/id (d/squuid)
                                :item/content "hei paa deg"
                                :item/checked false})

 (d/pull (d/db conn) '[:list/id
                       :list/type
                       {:list/items [:item/content
                                     :item/id]}] [:list/id "5"])

 (d/q '[:find [(pull ?e [:list/id :list/type]) ...]
        :where
        [?e :list/id ?id]
        [?e :list/item ?items]]
      (d/db conn))

 )
