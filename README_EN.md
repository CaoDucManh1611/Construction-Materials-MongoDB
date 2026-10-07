# Construction materials management — MongoDB

The application uses Java 21+, Spring Boot 4.0.6, Spring Data MongoDB, Spring Security and Thymeleaf. See [README.md](README.md) for the verified business workflow, schema, setup, demo accounts and scope.

Run `docker compose up -d --wait` at the repository root, then run Maven from `ht_vlxd` with `MONGODB_URI=mongodb://localhost:27017/vlxd_db?replicaSet=rs0`. Enable `SEED_DEMO=true` only for a fresh demo database. Money and stock transactions require a replica set.


Integration tests require an isolated MongoDB replica set via `TEST_MONGODB_URI`; never use production data for tests. JavaScript syntax and MongoDB structure checks are under `scripts`. See [Database.mongodb.md](Database.mongodb.md) for the collections, BSON fields, references and indexes, and [Database.mongodb.js](Database.mongodb.js) for initialization with mongosh.
