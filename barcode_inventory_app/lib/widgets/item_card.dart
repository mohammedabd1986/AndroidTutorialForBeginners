part of 'package:barcode_inventory_app/main.dart';

class ItemCard extends StatelessWidget {
  final InventoryItem item;

  const ItemCard({super.key, required this.item});

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 3,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              item.itemNameAr,
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            const SizedBox(height: 8),
            Text('الباركود: ${item.barcode}'),
            Text('السعر: ${item.price.toStringAsFixed(2)}'),
            Text('الكمية المتاحة: ${item.qty}'),
          ],
        ),
      ),
    );
  }
}
