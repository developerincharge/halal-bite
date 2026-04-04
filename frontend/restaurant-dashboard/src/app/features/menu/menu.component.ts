// src/app/features/menu/menu.component.ts
import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RestaurantService, MenuService } from '../../core/services/api.service';
import { Restaurant, MenuCategory, MenuItem } from '../../shared/models';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="page">
      <div class="page-header">
        <div>
          <h1 class="page-title">Menu Management</h1>
          <p class="page-sub">Manage your categories and food items.</p>
        </div>
      </div>

      @if (!restaurant()) {
        <!-- No restaurant — show setup form -->
        <div class="card">
          <h2>Set Up Your Restaurant</h2>
          <p class="form-hint">Fill in your restaurant details to get started on the platform.</p>
          <form [formGroup]="restaurantForm" (ngSubmit)="createRestaurant()" class="form-grid">
            <div class="field">
              <label>Restaurant Name *</label>
              <input formControlName="name" placeholder="e.g. Halal Burgers Chicago" />
            </div>
            <div class="field">
              <label>Cuisine Type *</label>
              <input formControlName="cuisineType" placeholder="e.g. American, Middle Eastern" />
            </div>
            <div class="field">
              <label>Street Address *</label>
              <input formControlName="streetAddress" placeholder="123 Main Street" />
            </div>
            <div class="field">
              <label>City *</label>
              <input formControlName="city" placeholder="Chicago" />
            </div>
            <div class="field">
              <label>Postal Code *</label>
              <input formControlName="postalCode" placeholder="60601" />
            </div>
            <div class="field">
              <label>Phone Number *</label>
              <input formControlName="phoneNumber" placeholder="312-555-0100" />
            </div>
            @if (formError()) {
              <div class="alert-error" style="grid-column: 1/-1">{{ formError() }}</div>
            }
            <button type="submit" class="btn-primary" style="grid-column: 1/-1"
              [disabled]="restaurantForm.invalid || saving()">
              {{ saving() ? 'Creating...' : 'Create Restaurant' }}
            </button>
          </form>
        </div>
      } @else {
        <!-- Two-column layout: categories left, items right -->
        <div class="menu-layout">

          <!-- Categories panel -->
          <div class="card">
            <div class="panel-header">
              <h2>Categories</h2>
              <button class="btn-sm" (click)="showCategoryForm.set(!showCategoryForm())">
                {{ showCategoryForm() ? '✕ Cancel' : '+ Add' }}
              </button>
            </div>

            @if (showCategoryForm()) {
              <form [formGroup]="categoryForm" (ngSubmit)="createCategory()" class="inline-form">
                <input formControlName="name" placeholder="Category name (e.g. Burgers)" />
                <button type="submit" class="btn-primary-sm" [disabled]="saving()">
                  {{ saving() ? '...' : 'Add' }}
                </button>
              </form>
            }

            <div class="category-list">
              @for (cat of categories(); track cat.id) {
                <div class="category-item"
                  [class.selected]="selectedCategory()?.id === cat.id"
                  (click)="selectCategory(cat)">
                  <span>{{ cat.name }}</span>
                  <span class="item-count">{{ cat.items.length }}</span>
                </div>
              } @empty {
                <div class="empty-small">No categories yet.</div>
              }
            </div>
          </div>

          <!-- Items panel -->
          <div class="card">
            @if (selectedCategory()) {
              <div class="panel-header">
                <h2>{{ selectedCategory()!.name }}</h2>
                <button class="btn-sm" (click)="showItemForm.set(!showItemForm())">
                  {{ showItemForm() ? '✕ Cancel' : '+ Add Item' }}
                </button>
              </div>

              @if (showItemForm()) {
                <form [formGroup]="itemForm" (ngSubmit)="createItem()" class="item-form">
                  <div class="form-grid-2">
                    <div class="field">
                      <label>Name *</label>
                      <input formControlName="name" placeholder="Big Halal Burger" />
                    </div>
                    <div class="field">
                      <label>Price *</label>
                      <input type="number" formControlName="price" placeholder="12.99" step="0.01" />
                    </div>
                    <div class="field" style="grid-column:1/-1">
                      <label>Description</label>
                      <input formControlName="description" placeholder="Juicy beef patty with fresh vegetables..." />
                    </div>
                    <div class="field">
                      <label>Calories</label>
                      <input type="number" formControlName="calories" placeholder="650" />
                    </div>
                    <div class="field">
                      <label>Prep time (min)</label>
                      <input type="number" formControlName="preparationTimeMinutes" placeholder="15" />
                    </div>
                  </div>
                  <div class="checkbox-row">
                    <label><input type="checkbox" formControlName="isSpicy" /> Spicy 🌶️</label>
                    <label><input type="checkbox" formControlName="isVegan" /> Vegan 🌿</label>
                    <label><input type="checkbox" formControlName="isGlutenFree" /> Gluten Free</label>
                  </div>
                  <button type="submit" class="btn-primary" [disabled]="itemForm.invalid || saving()">
                    {{ saving() ? 'Adding...' : 'Add Item' }}
                  </button>
                </form>
              }

              <div class="items-list">
                @for (item of selectedCategory()!.items; track item.id) {
                  <div class="item-card" [class.unavailable]="!item.isAvailable">
                    <div class="item-info">
                      <div class="item-name">{{ item.name }}</div>
                      <div class="item-desc">{{ item.description }}</div>
                      <div class="item-badges">
                        @if (item.isSpicy) { <span class="badge">🌶️ Spicy</span> }
                        @if (item.isVegan) { <span class="badge">🌿 Vegan</span> }
                        @if (item.calories) { <span class="badge">{{ item.calories }} cal</span> }
                      </div>
                    </div>
                    <div class="item-right">
                      <div class="item-price">\${{ item.price | number:'1.2-2' }}</div>
                      <button class="toggle-btn"
                        [class.available]="item.isAvailable"
                        (click)="toggleAvailability(item)">
                        {{ item.isAvailable ? 'Available' : 'Unavailable' }}
                      </button>
                    </div>
                  </div>
                } @empty {
                  <div class="empty-small">No items in this category yet.</div>
                }
              </div>
            } @else {
              <div class="empty-panel">
                <p>← Select a category to manage its items</p>
              </div>
            }
          </div>

        </div>
      }
    </div>
  `,
  styles: [`
    .page { max-width: 1100px; margin: 0 auto; }
    .page-header { margin-bottom: 28px; }
    .page-title { font-size: 26px; font-weight: 700; color: #1a1a2e; margin: 0 0 4px; }
    .page-sub { color: #666; margin: 0; font-size: 14px; }

    .card { background: white; border-radius: 12px; padding: 24px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
    .card h2 { font-size: 16px; font-weight: 700; color: #1a1a2e; margin: 0; }

    .form-hint { color: #666; font-size: 13px; margin: 4px 0 20px; }

    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    .form-grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 12px; }

    .field { display: flex; flex-direction: column; gap: 6px; }
    .field label { font-size: 12px; font-weight: 600; color: #444; }
    .field input, input[type=text], input[type=number] {
      padding: 10px 12px; border: 1.5px solid #e0e0e0;
      border-radius: 8px; font-size: 14px; outline: none;
      transition: border-color 0.15s; width: 100%; box-sizing: border-box;
    }
    .field input:focus { border-color: #f4a261; }

    .btn-primary {
      padding: 12px 20px; background: #f4a261; color: white;
      border: none; border-radius: 8px; font-size: 14px;
      font-weight: 600; cursor: pointer;
    }
    .btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
    .btn-sm {
      padding: 6px 12px; font-size: 12px; font-weight: 600;
      background: #f8f9fa; color: #333; border: 1px solid #e0e0e0;
      border-radius: 6px; cursor: pointer;
    }
    .btn-primary-sm {
      padding: 8px 14px; background: #f4a261; color: white;
      border: none; border-radius: 6px; font-size: 13px;
      font-weight: 600; cursor: pointer;
    }

    .alert-error {
      background: #fff5f5; color: #dc3545;
      padding: 10px 14px; border-radius: 8px;
      font-size: 13px; border: 1px solid #fecaca;
    }

    .menu-layout { display: grid; grid-template-columns: 280px 1fr; gap: 20px; }

    .panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }

    .inline-form { display: flex; gap: 8px; margin-bottom: 16px; }
    .inline-form input { flex: 1; padding: 8px 12px; border: 1.5px solid #e0e0e0; border-radius: 8px; font-size: 13px; outline: none; }

    .category-list { display: flex; flex-direction: column; gap: 4px; }
    .category-item {
      display: flex; justify-content: space-between; align-items: center;
      padding: 10px 12px; border-radius: 8px; cursor: pointer;
      font-size: 14px; transition: background 0.15s; color: #333;
    }
    .category-item:hover { background: #f8f9fa; }
    .category-item.selected { background: #fff3e0; color: #e76f51; font-weight: 600; }
    .item-count { font-size: 11px; background: #e0e0e0; padding: 2px 7px; border-radius: 10px; color: #666; }

    .empty-small { color: #aaa; font-size: 13px; padding: 16px 0; text-align: center; }

    .item-form { margin-bottom: 20px; padding-bottom: 20px; border-bottom: 1px solid #eee; }
    .checkbox-row { display: flex; gap: 16px; margin-bottom: 16px; font-size: 13px; }
    .checkbox-row label { display: flex; align-items: center; gap: 6px; cursor: pointer; }

    .items-list { display: flex; flex-direction: column; gap: 10px; }
    .item-card {
      display: flex; justify-content: space-between; align-items: flex-start;
      padding: 14px; border: 1px solid #eee; border-radius: 10px;
    }
    .item-card.unavailable { opacity: 0.5; }
    .item-info { flex: 1; }
    .item-name { font-size: 14px; font-weight: 600; color: #1a1a2e; margin-bottom: 4px; }
    .item-desc { font-size: 12px; color: #888; margin-bottom: 8px; }
    .item-badges { display: flex; gap: 6px; flex-wrap: wrap; }
    .badge { font-size: 11px; background: #f0f0f0; padding: 2px 8px; border-radius: 10px; color: #555; }
    .item-right { display: flex; flex-direction: column; align-items: flex-end; gap: 8px; margin-left: 16px; }
    .item-price { font-size: 16px; font-weight: 700; color: #1a1a2e; }
    .toggle-btn {
      font-size: 11px; padding: 4px 10px; border-radius: 20px;
      border: none; cursor: pointer; font-weight: 600;
      background: #f8d7da; color: #721c24;
    }
    .toggle-btn.available { background: #d4edda; color: #155724; }

    .empty-panel {
      display: flex; align-items: center; justify-content: center;
      height: 200px; color: #aaa; font-size: 14px;
    }
  `]
})
export class MenuComponent implements OnInit {
  private restaurantService = inject(RestaurantService);
  private menuService = inject(MenuService);
  private fb = inject(FormBuilder);

  restaurant = signal<Restaurant | null>(null);
  categories = signal<MenuCategory[]>([]);
  selectedCategory = signal<MenuCategory | null>(null);
  showCategoryForm = signal(false);
  showItemForm = signal(false);
  saving = signal(false);
  formError = signal('');

  restaurantForm = this.fb.group({
    name: ['', Validators.required],
    cuisineType: ['', Validators.required],
    streetAddress: ['', Validators.required],
    city: ['', Validators.required],
    postalCode: ['', Validators.required],
    phoneNumber: ['', Validators.required]
  });

  categoryForm = this.fb.group({
    name: ['', Validators.required]
  });

  itemForm = this.fb.group({
    name: ['', Validators.required],
    price: [null, [Validators.required, Validators.min(0.01)]],
    description: [''],
    calories: [null],
    preparationTimeMinutes: [15],
    isSpicy: [false],
    isVegan: [false],
    isGlutenFree: [false]
  });

  ngOnInit() {
    this.restaurantService.getMyRestaurant().subscribe({
      next: (restaurants) => {
        if (restaurants.length > 0) {
          this.restaurant.set(restaurants[0]);
          this.loadMenu(restaurants[0].id);
        }
      }
    });
  }

  loadMenu(restaurantId: string) {
    this.menuService.getCategories(restaurantId).subscribe({
      next: (cats) => {
        this.categories.set(cats);
        if (cats.length > 0 && !this.selectedCategory()) {
          this.selectedCategory.set(cats[0]);
        }
      }
    });
  }

  createRestaurant() {
    if (this.restaurantForm.invalid) return;
    this.saving.set(true);
    this.restaurantService.createRestaurant(this.restaurantForm.value as any).subscribe({
      next: (r) => { this.restaurant.set(r); this.saving.set(false); },
      error: (err) => {
        this.formError.set(err.error?.message ?? 'Failed to create restaurant');
        this.saving.set(false);
      }
    });
  }

  createCategory() {
    if (!this.restaurant() || this.categoryForm.invalid) return;
    this.saving.set(true);
    this.menuService.createCategory(this.restaurant()!.id, this.categoryForm.value as any).subscribe({
      next: () => {
        this.categoryForm.reset();
        this.showCategoryForm.set(false);
        this.saving.set(false);
        this.loadMenu(this.restaurant()!.id);
      },
      error: () => this.saving.set(false)
    });
  }

  selectCategory(cat: MenuCategory) {
    this.selectedCategory.set(cat);
    this.showItemForm.set(false);
  }

  createItem() {
    if (!this.restaurant() || !this.selectedCategory() || this.itemForm.invalid) return;
    this.saving.set(true);
    this.menuService.createItem(this.restaurant()!.id, {
      ...this.itemForm.value as any,
      categoryId: this.selectedCategory()!.id
    }).subscribe({
      next: () => {
        this.itemForm.reset({ preparationTimeMinutes: 15, isSpicy: false, isVegan: false, isGlutenFree: false });
        this.showItemForm.set(false);
        this.saving.set(false);
        this.loadMenu(this.restaurant()!.id);
      },
      error: () => this.saving.set(false)
    });
  }

  toggleAvailability(item: MenuItem) {
    this.menuService.toggleAvailability(this.restaurant()!.id, item.id).subscribe({
      next: () => this.loadMenu(this.restaurant()!.id)
    });
  }
}
