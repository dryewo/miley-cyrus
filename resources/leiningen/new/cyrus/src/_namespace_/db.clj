(ns {{namespace}}.db
  (:require [conman.core :as conman]
            [mount.core :as m]
            [schema.core :as s]
            [migratus.core :as migratus]
            [clojure.java.jdbc :as jdbc]
            [squeeze.core :as squeeze]
            [{{namespace}}.lib.db]
            [{{namespace}}.env :as env]))

(def config-defaults
  {:db-jdbc-url "jdbc:postgresql://localhost:5432/postgres"
   :db-username "postgres"})

(s/defschema Config
  {(s/optional-key :db-jdbc-url) s/Str
   (s/optional-key :db-username) s/Str
   (s/optional-key :db-password) s/Str})

(def migratus-config
  {:store         :database
   :migration-dir "db/migrations"})

(m/defstate ^:dynamic *db*
  :start (let [config (squeeze/coerce-config Config (merge config-defaults @env/env))
               db     (conman/connect! (squeeze/remove-key-prefix :db- config))]
           (migratus/migrate (merge migratus-config {:db db}))
           db)
  :stop (conman/disconnect! @*db*))

(conman/bind-connection *db* "db/queries.sql")

(comment
  ;; CRUD operations
  (create-memory! {:id "1" :memory-text "foo"})
  (get-memory {:id "1"})
  (update-memory! {:id "1" :memory-text "bar"})
  (delete-memory! {:id "1"})

  ;; Transaction example
  (conman/with-transaction [*db*]
    (jdbc/db-set-rollback-only! @*db*)
    (create-memory! {:id "2" :memory-text "foo"})
    (get-memory {:id "2"}))
  (get-memory {:id "2"})

  ;; Helper to create new migrations
  (migratus/create migratus-config "create-user")
  )