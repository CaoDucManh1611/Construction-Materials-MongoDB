# Construction materials management — MongoDB

The application uses Java 21+, Spring Boot 4.0.6, Spring Data MongoDB, Spring Security and Thymeleaf. MySQL/JPA runtime persistence has been replaced with MongoDB. See [README.md](README.md) for the verified business workflow, schema, setup, demo accounts and scope.

Run `docker compose up -d --wait` at the repository root, then run Maven from `ht_vlxd` with `MONGODB_URI=mongodb://localhost:27017/vlxd_db?replicaSet=rs0`. Enable `SEED_DEMO=true` only for a fresh demo database. Money and stock transactions require a replica set.

The offline `scripts/convert_mysql_export.py` prepares Extended JSON from a JSON export of the old tables; it does not execute SQL or import into a database. Historical balances require reconciliation because the old workflows could fabricate deposits or deduct inventory twice.

Integration tests require an isolated MongoDB replica set via `TEST_MONGODB_URI`; never use production data for tests. JavaScript syntax and offline conversion checks are under `scripts`.
