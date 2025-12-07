part of 'package:barcode_inventory_app/main.dart';

class HomeScreen extends StatefulWidget {
  final LocalStore store;

  const HomeScreen({super.key, required this.store});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with SingleTickerProviderStateMixin {
  InventoryItem? _selectedItem;
  String? _lastBarcode;
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
  }

  void _handleBarcode(String barcode) {
    final item = widget.store.findByBarcode(barcode);
    setState(() {
      _lastBarcode = barcode;
      _selectedItem = item;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('مخزن الباركود'),
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(text: 'المسح والبحث'),
            Tab(text: 'المزامنة'),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _buildSearchTab(context),
          SyncScreen(store: widget.store),
        ],
      ),
    );
  }

  Widget _buildSearchTab(BuildContext context) {
    final TextEditingController controller = TextEditingController();

    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          ElevatedButton.icon(
            icon: const Icon(Icons.qr_code_scanner),
            label: const Text('مسح الباركود'),
            onPressed: () async {
              final code = await Navigator.of(context).push<String>(
                MaterialPageRoute(builder: (_) => const ScanScreen()),
              );
              if (code != null) {
                _handleBarcode(code);
              }
            },
          ),
          const SizedBox(height: 12),
          TextField(
            controller: controller,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(
              labelText: 'بحث يدوي عن الباركود',
              border: OutlineInputBorder(),
            ),
            onSubmitted: (value) {
              if (value.trim().isNotEmpty) {
                _handleBarcode(value.trim());
              }
            },
          ),
          const SizedBox(height: 16),
          if (_lastBarcode != null)
            Text('آخر باركود: $_lastBarcode',
                style: Theme.of(context).textTheme.bodyMedium),
          const SizedBox(height: 12),
          Expanded(
            child: ValueListenableBuilder<List<InventoryItem>>(
              valueListenable: widget.store.inventoryNotifier,
              builder: (context, items, _) {
                if (_selectedItem != null) {
                  return ItemCard(item: _selectedItem!);
                }
                if (_lastBarcode == null) {
                  return Center(
                    child: Text(
                      'ابدأ بالمزامنة ثم ابحث عن الباركود أو امسحه.',
                      style: Theme.of(context).textTheme.titleMedium,
                      textAlign: TextAlign.center,
                    ),
                  );
                }
                return Center(
                  child: Text(
                    'الباركود غير موجود في قاعدة البيانات',
                    style: Theme.of(context)
                        .textTheme
                        .titleMedium
                        ?.copyWith(color: Colors.red),
                    textAlign: TextAlign.center,
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
