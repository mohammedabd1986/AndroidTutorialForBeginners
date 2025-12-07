part of 'package:barcode_inventory_app/main.dart';

@HiveType(typeId: 1)
class InventoryItem {
  @HiveField(0)
  final String barcode;

  @HiveField(1)
  final String itemNameAr;

  @HiveField(2)
  final double price;

  @HiveField(3)
  final int qty;

  const InventoryItem({
    required this.barcode,
    required this.itemNameAr,
    required this.price,
    required this.qty,
  });

  factory InventoryItem.fromJson(Map<String, dynamic> json) {
    return InventoryItem(
      barcode: json['barcode']?.toString() ?? '',
      itemNameAr: json['item_name_ar']?.toString() ?? 'غير معروف',
      price: double.tryParse(json['price']?.toString() ?? '0') ?? 0,
      qty: int.tryParse(json['qty']?.toString() ?? '0') ?? 0,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'barcode': barcode,
      'item_name_ar': itemNameAr,
      'price': price,
      'qty': qty,
    };
  }
}
