// Erzeugt aus HANDOUT.md eine echte DOCX-Datei (OOXML) ohne externe Bibliotheken.
const fs = require('fs');
const zlib = require('zlib');

const md = fs.readFileSync('HANDOUT.md', 'utf8').replace(/\r\n/g, '\n');

const esc = s => String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

function run(text, o = {}) {
  if (text === '') return '';
  let rpr = '';
  const font = o.code ? 'Consolas' : o.font;
  if (font) rpr += '<w:rFonts w:ascii="' + font + '" w:hAnsi="' + font + '" w:cs="' + font + '"/>';
  if (o.b) rpr += '<w:b/>';
  if (o.i) rpr += '<w:i/>';
  if (o.color) rpr += '<w:color w:val="' + o.color + '"/>';
  if (o.sz) rpr += '<w:sz w:val="' + o.sz + '"/><w:szCs w:val="' + o.sz + '"/>';
  const pr = rpr ? '<w:rPr>' + rpr + '</w:rPr>' : '';
  const parts = String(text).split('\n');
  let inner = '';
  for (let k = 0; k < parts.length; k++) {
    if (k > 0) inner += '<w:br/>';
    inner += '<w:t xml:space="preserve">' + esc(parts[k]) + '</w:t>';
  }
  return '<w:r>' + pr + inner + '</w:r>';
}

function para(content, o = {}) {
  let ppr = '';
  if (o.style) ppr += '<w:pStyle w:val="' + o.style + '"/>';
  if (o.keepNext) ppr += '<w:keepNext/>';
  if (o.border) ppr += '<w:pBdr><w:left w:val="single" w:sz="18" w:space="8" w:color="2563EB"/></w:pBdr>';
  if (o.shd) ppr += '<w:shd w:val="clear" w:color="auto" w:fill="' + o.shd + '"/>';
  if (o.ind !== undefined) ppr += '<w:ind w:left="' + o.ind + '"' + (o.hanging ? ' w:hanging="' + o.hanging + '"' : '') + '/>';
  ppr += '<w:spacing w:before="' + (o.before || 0) + '" w:after="' + (o.after === undefined ? 100 : o.after) + '" w:line="' + (o.line || 276) + '" w:lineRule="auto"/>';
  return '<w:p>' + (ppr ? '<w:pPr>' + ppr + '</w:pPr>' : '') + content + '</w:p>';
}

function inline(text, base = {}) {
  const out = [];
  const re = /(`[^`]*`|\*\*[^*]+\*\*)/g;
  let last = 0, m;
  while ((m = re.exec(text)) !== null) {
    if (m.index > last) out.push(run(text.slice(last, m.index), base));
    const tok = m[0];
    if (tok.charAt(0) === '`') out.push(run(tok.slice(1, -1), Object.assign({}, base, { code: true })));
    else out.push(run(tok.slice(2, -2), Object.assign({}, base, { b: true })));
    last = m.index + tok.length;
  }
  if (last < text.length) out.push(run(text.slice(last), base));
  return out.join('');
}

function pickWidths(header) {
  if (header.length === 3) {
    const h0 = (header[0] || '').trim();
    if (h0 === 'Status') return [1200, 3500, 4938];
    if (h0 === 'Bereich') return [1900, 3500, 4238];
    if (h0 === '') return [2600, 3519, 3519];
    return [3000, 3319, 3319];
  }
  if (header.length === 2) return [3200, 6438];
  const w = Math.floor(9638 / header.length);
  return header.map(() => w);
}

function table(rows) {
  const widths = pickWidths(rows[0]);
  const total = widths.reduce((a, b) => a + b, 0);
  const b = 'w:val="single" w:sz="4" w:space="0" w:color="CBD5E1"';
  let xml = '<w:tbl><w:tblPr><w:tblW w:type="dxa" w:w="' + total + '"/><w:tblLayout w:type="fixed"/>' +
    '<w:tblBorders><w:top ' + b + '/><w:left ' + b + '/><w:bottom ' + b + '/><w:right ' + b + '/><w:insideH ' + b + '/><w:insideV ' + b + '/></w:tblBorders>' +
    '<w:tblCellMar><w:top w:w="60" w:type="dxa"/><w:left w:w="100" w:type="dxa"/><w:bottom w:w="60" w:type="dxa"/><w:right w:w="100" w:type="dxa"/></w:tblCellMar></w:tblPr>';
  xml += '<w:tblGrid>' + widths.map(w => '<w:gridCol w:w="' + w + '"/>').join('') + '</w:tblGrid>';
  rows.forEach((cells, ri) => {
    const hdr = ri === 0;
    xml += '<w:tr>' + (hdr ? '<w:trPr><w:tblHeader/></w:trPr>' : '');
    cells.forEach((c, ci) => {
      const shd = hdr ? '<w:shd w:val="clear" w:color="auto" w:fill="EFF6FF"/>' : '';
      xml += '<w:tc><w:tcPr><w:tcW w:w="' + (widths[ci] || widths[widths.length - 1]) + '" w:type="dxa"/>' + shd + '<w:vAlign w:val="top"/></w:tcPr>';
      xml += para(inline(c, hdr ? { b: true } : {}), { after: 20, line: 240 });
      xml += '</w:tc>';
    });
    xml += '</w:tr>';
  });
  return xml + '</w:tbl>' + para('', { after: 120 });
}

const lines = md.split('\n');
let idx = 0;
let body = '';

function isBlockStart(l) {
  return /^```/.test(l) || /^#{1,3} /.test(l) || /^\|/.test(l) || /^> /.test(l) ||
    /^\s*[-*] /.test(l) || /^\s*\d+\. /.test(l) || /^---+\s*$/.test(l) || l.trim() === '';
}

while (idx < lines.length) {
  const line = lines[idx];

  if (line.trim() === '' || /^---+\s*$/.test(line)) { idx++; continue; }

  let m;
  if (/^```/.test(line)) {
    idx++;
    const code = [];
    while (idx < lines.length && !/^```/.test(lines[idx])) { code.push(lines[idx]); idx++; }
    idx++;
    body += para(run(code.join('\n'), { code: true, sz: 17 }), { shd: 'F8FAFC', ind: 120, before: 60, after: 140, line: 240 });
    continue;
  }

  if ((m = line.match(/^(#{1,3}) (.*)$/))) {
    const lvl = m[1].length;
    const style = lvl === 1 ? 'Title' : lvl === 2 ? 'Heading1' : 'Heading2';
    body += para(run(m[2]), { style });
    idx++;
    continue;
  }

  if (/^\|/.test(line)) {
    const rows = [];
    while (idx < lines.length && /^\|/.test(lines[idx])) {
      const raw = lines[idx].trim();
      if (!/^\|[\s:|-]+\|$/.test(raw)) {
        rows.push(raw.replace(/^\|/, '').replace(/\|$/, '').split('|').map(s => s.trim()));
      }
      idx++;
    }
    if (rows.length) body += table(rows);
    continue;
  }

  if (/^> /.test(line)) {
    const quote = [];
    while (idx < lines.length && /^> /.test(lines[idx])) {
      quote.push(lines[idx].replace(/^> ?/, ''));
      idx++;
    }
    body += para(inline(quote.join(' '), { i: true }), { ind: 240, border: true, shd: 'EFF6FF', after: 140 });
    continue;
  }

  if ((m = line.match(/^(\s*)- (.*)$/))) {
    const level = Math.floor(m[1].length / 2);
    const ind = 300 + level * 360;
    body += para(run(level === 0 ? '\u2022  ' : '\u2013  ') + inline(m[2]), { ind, hanging: 240, after: 50 });
    idx++;
    continue;
  }

  if ((m = line.match(/^(\s*)(\d+)\. (.*)$/))) {
    const level = Math.floor(m[1].length / 2);
    const ind = 300 + level * 360;
    body += para(run(m[2] + '.  ') + inline(m[3]), { ind, hanging: 240, after: 50 });
    idx++;
    continue;
  }

  if (/^\*\*(Projekt|Team|Technik|Zweig):\*\*/.test(line)) {
    body += para(inline(line), { after: 20 });
    idx++;
    continue;
  }

  const buf = [line.trim()];
  idx++;
  while (idx < lines.length && !isBlockStart(lines[idx])) {
    buf.push(lines[idx].trim());
    idx++;
  }
  body += para(inline(buf.join(' ')));
}

const documentXml = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' +
  '<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">' +
  '<w:body>' + body +
  '<w:sectPr><w:pgSz w:w="11906" w:h="16838"/>' +
  '<w:pgMar w:top="1134" w:right="1134" w:bottom="1134" w:left="1134" w:header="708" w:footer="708" w:gutter="0"/>' +
  '<w:cols w:space="708"/></w:sectPr></w:body></w:document>';

const stylesXml = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' +
  '<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">' +
  '<w:docDefaults><w:rPrDefault><w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:cs="Calibri"/>' +
  '<w:sz w:val="21"/><w:szCs w:val="21"/><w:lang w:val="de-DE"/></w:rPr></w:rPrDefault>' +
  '<w:pPrDefault><w:pPr><w:spacing w:after="100" w:line="276" w:lineRule="auto"/></w:pPr></w:pPrDefault></w:docDefaults>' +
  '<w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/><w:qFormat/></w:style>' +
  '<w:style w:type="paragraph" w:styleId="Title"><w:name w:val="Title"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:qFormat/>' +
  '<w:pPr><w:spacing w:before="0" w:after="120"/></w:pPr>' +
  '<w:rPr><w:b/><w:color w:val="1D4ED8"/><w:sz w:val="42"/><w:szCs w:val="42"/></w:rPr></w:style>' +
  '<w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:qFormat/>' +
  '<w:pPr><w:keepNext/><w:spacing w:before="280" w:after="100"/><w:outlineLvl w:val="0"/></w:pPr>' +
  '<w:rPr><w:b/><w:color w:val="1D4ED8"/><w:sz w:val="28"/><w:szCs w:val="28"/></w:rPr></w:style>' +
  '<w:style w:type="paragraph" w:styleId="Heading2"><w:name w:val="heading 2"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:qFormat/>' +
  '<w:pPr><w:keepNext/><w:spacing w:before="220" w:after="80"/><w:outlineLvl w:val="1"/></w:pPr>' +
  '<w:rPr><w:b/><w:color w:val="0F172A"/><w:sz w:val="23"/><w:szCs w:val="23"/></w:rPr></w:style>' +
  '</w:styles>';

const contentTypes = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' +
  '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">' +
  '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>' +
  '<Default Extension="xml" ContentType="application/xml"/>' +
  '<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>' +
  '<Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>' +
  '</Types>';

const rootRels = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' +
  '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">' +
  '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>' +
  '</Relationships>';

const docRels = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' +
  '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">' +
  '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>' +
  '</Relationships>';

// --- ZIP ohne externe Bibliothek ---
const crcTable = (() => {
  const t = new Int32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xEDB88320 ^ (c >>> 1) : c >>> 1;
    t[n] = c;
  }
  return t;
})();

function crc32(buf) {
  let c = 0xFFFFFFFF;
  for (let i = 0; i < buf.length; i++) c = crcTable[(c ^ buf[i]) & 0xFF] ^ (c >>> 8);
  return (c ^ 0xFFFFFFFF) >>> 0;
}

function zip(files) {
  const chunks = [], central = [];
  let offset = 0;
  for (const f of files) {
    const name = Buffer.from(f.name, 'utf8');
    const data = Buffer.from(f.data, 'utf8');
    const comp = zlib.deflateRawSync(data, { level: 9 });
    const crc = crc32(data);
    const local = Buffer.alloc(30);
    local.writeUInt32LE(0x04034b50, 0);
    local.writeUInt16LE(20, 4);
    local.writeUInt16LE(0x0800, 6);
    local.writeUInt16LE(8, 8);
    local.writeUInt16LE(0, 10);
    local.writeUInt16LE(0, 12);
    local.writeUInt32LE(crc, 14);
    local.writeUInt32LE(comp.length, 18);
    local.writeUInt32LE(data.length, 22);
    local.writeUInt16LE(name.length, 26);
    local.writeUInt16LE(0, 28);
    chunks.push(local, name, comp);

    const cd = Buffer.alloc(46);
    cd.writeUInt32LE(0x02014b50, 0);
    cd.writeUInt16LE(20, 4);
    cd.writeUInt16LE(20, 6);
    cd.writeUInt16LE(0x0800, 8);
    cd.writeUInt16LE(8, 10);
    cd.writeUInt16LE(0, 12);
    cd.writeUInt16LE(0, 14);
    cd.writeUInt32LE(crc, 16);
    cd.writeUInt32LE(comp.length, 20);
    cd.writeUInt32LE(data.length, 24);
    cd.writeUInt16LE(name.length, 28);
    cd.writeUInt16LE(0, 30);
    cd.writeUInt16LE(0, 32);
    cd.writeUInt16LE(0, 34);
    cd.writeUInt16LE(0, 36);
    cd.writeUInt32LE(0, 38);
    cd.writeUInt32LE(offset, 42);
    central.push(cd, name);

    offset += local.length + name.length + comp.length;
  }
  const centralBuf = Buffer.concat(central);
  const eocd = Buffer.alloc(22);
  eocd.writeUInt32LE(0x06054b50, 0);
  eocd.writeUInt16LE(0, 4);
  eocd.writeUInt16LE(0, 6);
  eocd.writeUInt16LE(files.length, 8);
  eocd.writeUInt16LE(files.length, 10);
  eocd.writeUInt32LE(centralBuf.length, 12);
  eocd.writeUInt32LE(offset, 16);
  eocd.writeUInt16LE(0, 20);
  return Buffer.concat(chunks.concat([centralBuf, eocd]));
}

const out = zip([
  { name: '[Content_Types].xml', data: contentTypes },
  { name: '_rels/.rels', data: rootRels },
  { name: 'word/document.xml', data: documentXml },
  { name: 'word/styles.xml', data: stylesXml },
  { name: 'word/_rels/document.xml.rels', data: docRels },
]);

fs.writeFileSync('HANDOUT.docx', out);
console.log('DOCX_OK bytes=' + out.length + ' paragraphs=' + (body.match(/<w:p>/g) || []).length);
