const fs = require('fs');

const fileToReplaceExports = fs.readFileSync(process.argv[2], "utf8");

fs.writeFileSync(
    process.argv[2],
    fileToReplaceExports.replace(/^import \* as (?!jspb).* from/gm, "export * from"),
    "utf8"
);

