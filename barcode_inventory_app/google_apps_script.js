/**
 * Google Apps Script endpoint to expose all rows from a Google Sheet as JSON.
 * Expected columns: barcode | item_name_ar | price | qty
 */
function doGet() {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName('Sheet1');
  var data = sheet.getDataRange().getValues();
  var headers = data.shift();
  var output = data.map(function(row) {
    var entry = {};
    headers.forEach(function(key, index) {
      entry[key] = row[index];
    });
    return entry;
  });

  return ContentService
    .createTextOutput(JSON.stringify(output))
    .setMimeType(ContentService.MimeType.JSON);
}
