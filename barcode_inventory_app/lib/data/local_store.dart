part of 'package:barcode_inventory_app/main.dart';

class LocalStore {
  late Box _box;
  final ValueNotifier<List<InventoryItem>> inventoryNotifier =
      ValueNotifier<List<InventoryItem>>([]);

  Future<void> init() async {
    _box = await Hive.openBox('inventory_box');
    final stored = _box.get('items') as List?;
    if (stored != null) {
      inventoryNotifier.value = stored
          .map((e) => InventoryItem.fromJson(Map<String, dynamic>.from(e)))
          .toList();
    }
  }

  Future<void> saveItems(List<InventoryItem> items) async {
    await _box.put('items', items.map((e) => e.toJson()).toList());
    inventoryNotifier.value = items;
  }

  InventoryItem? findByBarcode(String barcode) {
    try {
      return inventoryNotifier.value
          .firstWhere((item) => item.barcode == barcode.trim());
    } catch (_) {
      return null;
    }
  }
}
