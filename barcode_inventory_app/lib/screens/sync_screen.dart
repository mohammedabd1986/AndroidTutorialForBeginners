part of 'package:barcode_inventory_app/main.dart';

class SyncScreen extends StatefulWidget {
  final LocalStore store;

  const SyncScreen({super.key, required this.store});

  @override
  State<SyncScreen> createState() => _SyncScreenState();
}

class _SyncScreenState extends State<SyncScreen> {
  final TextEditingController _urlController = TextEditingController(
    text: 'https://script.google.com/macros/s/YOUR_SCRIPT_ID/exec',
  );
  bool _loading = false;
  String? _message;

  Future<void> _sync() async {
    setState(() {
      _loading = true;
      _message = null;
    });

    try {
      final service = SheetsService(_urlController.text.trim());
      final items = await service.fetchInventory();
      await widget.store.saveItems(items);
      setState(() {
        _message = 'تم تحديث ${items.length} عنصر بنجاح';
      });
    } catch (e) {
      setState(() {
        _message = 'خطأ أثناء التحديث: $e';
      });
    } finally {
      setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          TextField(
            controller: _urlController,
            decoration: const InputDecoration(
              labelText: 'رابط Google Apps Script',
              border: OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 12),
          ElevatedButton.icon(
            onPressed: _loading ? null : _sync,
            icon: _loading
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Icon(Icons.sync),
            label: Text(_loading ? 'جاري التحديث...' : 'تحديث البيانات'),
          ),
          const SizedBox(height: 16),
          ValueListenableBuilder<List<InventoryItem>>(
            valueListenable: widget.store.inventoryNotifier,
            builder: (context, items, _) {
              return Text('عدد العناصر المخزنة: ${items.length}');
            },
          ),
          if (_message != null) ...[
            const SizedBox(height: 12),
            Text(
              _message!,
              style: Theme.of(context)
                  .textTheme
                  .bodyMedium
                  ?.copyWith(color: Colors.teal.shade700),
            ),
          ],
        ],
      ),
    );
  }
}
