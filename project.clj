(defproject datomic-demo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.datomic/datomic-free "0.9.5404"]
                 [org.clojure/clojure "1.8.0"]]
  :main ^:skip-aot datomic-demo.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
