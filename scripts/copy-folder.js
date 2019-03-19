const fs = require('fs');

// destination.txt will be created or overwritten by default.
fs.copyF(process.argv[2], process.argv[3], (err) => {
  if (err) throw err;
  console.log(`${process.argv[2]} was copied to ${process.argv[3]}`);
});