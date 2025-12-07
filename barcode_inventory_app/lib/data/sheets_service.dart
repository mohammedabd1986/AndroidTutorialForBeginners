part of 'package:barcode_inventory_app/main.dart';

class SheetsService {
  SheetsService(this.scriptUrl);

  final String scriptUrl;

  Future<List<InventoryItem>> fetchInventory() async {
    final response = await http.get(Uri.parse(scriptUrl));
    if (response.statusCode != 200) {
      throw Exception('فشل في جلب البيانات: ${response.statusCode}');
    }

    final decoded = json.decode(response.body);
    if (decoded is List) {
      return decoded.map((e) => InventoryItem.fromJson(Map<String, dynamic>.from(e))).toList();
    }

    throw Exception('الاستجابة غير متوقعة');
  }
}
