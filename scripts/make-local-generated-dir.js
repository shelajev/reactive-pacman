const fs = require('fs');

fs.mkdir('./src/generated/main/javascript', { recursive: true }, (err) => {
    if (err) throw err;
});