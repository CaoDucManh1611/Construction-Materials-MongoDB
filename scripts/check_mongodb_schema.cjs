// Offline check: init creates the expected structure and can run twice without deleting data.
const fs = require('node:fs');
const vm = require('node:vm');
const assert = require('node:assert/strict');
const path = require('node:path');
const source = fs.readFileSync(path.join(__dirname, '..', 'Database.mongodb.js'), 'utf8');
const names = new Set(['existing_collection']);
const indexes = new Map();
const db = {
  getName: () => 'schema_check',
  getCollectionNames: () => [...names],
  createCollection(name) { assert(!names.has(name)); names.add(name); },
  getCollection(name) {
    assert(names.has(name));
    return { createIndex(keys, options) {
      assert.equal(options.unique, true);
      const id = name + ':' + options.name;
      const spec = JSON.stringify({ keys, options });
      if (indexes.has(id)) assert.equal(indexes.get(id), spec);
      indexes.set(id, spec);
    }};
  }
};
for (let i = 0; i < 2; i++) vm.runInNewContext(source, { db, print() {} });
assert.equal(names.size, 23); // 22 project collections + pre-existing collection.
assert.equal(indexes.size, 24);
assert(!names.has('don_hang_chi_tiet'));
assert(!names.has('phieu_kho_chi_tiet'));
assert(![...indexes.keys()].some(k => k.startsWith('de_xuat_danh_muc:')));
assert(indexes.has('ton_kho:TonKho_unique'));
console.log('MongoDB schema: 22 collections, 24 indexes; repeated initialization passed (offline).');
