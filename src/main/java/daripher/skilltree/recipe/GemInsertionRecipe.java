package daripher.skilltree.recipe;

import com.google.gson.JsonObject;

import daripher.skilltree.api.PlayerContainer;
import daripher.skilltree.compat.apotheosis.ApotheosisCompatibility;
import daripher.skilltree.gem.GemHelper;
import daripher.skilltree.init.SkillTreeAttributes;
import daripher.skilltree.init.SkillTreeRecipeSerializers;
import daripher.skilltree.item.gem.GemItem;
import daripher.skilltree.util.ItemHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

public class GemInsertionRecipe extends UpgradeRecipe {
	public GemInsertionRecipe(ResourceLocation recipeId) {
		super(recipeId, Ingredient.EMPTY, Ingredient.EMPTY, ItemStack.EMPTY);
	}

	@Override
	public boolean matches(Container container, Level level) {
		if (ModList.get().isLoaded("apotheosis")) {
			if (ApotheosisCompatibility.ISNTANCE.adventureModuleEnabled()) {
				return false;
			}
		}
		if (!isBaseIngredient(container.getItem(0))) {
			return false;
		}
		if (!isAdditionIngredient(container.getItem(1))) {
			return false;
		}
		var gem = (GemItem) container.getItem(1).getItem();
		var playerContainer = (PlayerContainer) container;
		var player = playerContainer.getPlayer().orElseThrow(NullPointerException::new);
		var gemBonus = gem.getGemBonus(player, container.getItem(0));
		return gemBonus != null;
	}

	@Override
	public ItemStack assemble(Container container) {
		if (ModList.get().isLoaded("apotheosis")) {
			if (ApotheosisCompatibility.ISNTANCE.adventureModuleEnabled()) {
				return ItemStack.EMPTY;
			}
		}
		var playerContainer = (PlayerContainer) container;
		if (!playerContainer.getPlayer().isPresent()) {
			return ItemStack.EMPTY;
		}
		var player = playerContainer.getPlayer().get();
		var baseItem = container.getItem(0);
		var gemstoneSlot = getFirstEmptyGemstoneSlot(baseItem, player);
		var gemstoneItem = (GemItem) container.getItem(1).getItem();
		if (!gemstoneItem.canInsertInto(player, baseItem, gemstoneSlot)) {
			return ItemStack.EMPTY;
		}
		var resultItemStack = baseItem.copy();
		if (baseItem.getTag() != null) {
			resultItemStack.setTag(baseItem.getTag().copy());
		}
		var gemPower = getPlayerGemPower(player, baseItem);
		gemstoneItem.insertInto(player, resultItemStack, gemstoneSlot, gemPower);
		return resultItemStack;
	}

	public double getPlayerGemPower(Player player, ItemStack craftingItem) {
		var gemstoneStrength = player.getAttributeValue(SkillTreeAttributes.GEM_POWER.get()) - 1;
		var craftingArmor = ItemHelper.isArmor(craftingItem) || ItemHelper.isShield(craftingItem);
		var craftingWeapon = ItemHelper.isWeapon(craftingItem) || ItemHelper.isBow(craftingItem);
		if (craftingArmor) {
			var gemstoneStrengthInArmor = player.getAttributeValue(SkillTreeAttributes.GEM_POWER_IN_ARMOR.get()) - 1;
			gemstoneStrength += gemstoneStrengthInArmor;
		} else if (craftingWeapon) {
			var gemstoneStrengthInWeapon = player.getAttributeValue(SkillTreeAttributes.GEM_POWER_IN_WEAPON.get()) - 1;
			gemstoneStrength += gemstoneStrengthInWeapon;
		}
		return gemstoneStrength;
	}

	public int getFirstEmptyGemstoneSlot(ItemStack baseItem, Player player) {
		var maximumSlots = GemHelper.getMaximumSockets(baseItem, player);
		var gemstoneSlot = 0;
		for (int i = 0; i < maximumSlots; i++) {
			gemstoneSlot = i;
			if (!GemHelper.hasGem(baseItem, gemstoneSlot)) {
				break;
			}
		}
		return gemstoneSlot;
	}

	@Override
	public ItemStack getResultItem() {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	public boolean isBaseIngredient(ItemStack itemStack) {
		return ItemHelper.canInsertGem(itemStack);
	}

	public boolean isAdditionIngredient(ItemStack itemStack) {
		return itemStack.getItem() instanceof GemItem;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SkillTreeRecipeSerializers.GEMSTONE_INSERTION.get();
	}

	@Override
	public boolean isIncomplete() {
		return false;
	}

	public static class Serializer implements RecipeSerializer<GemInsertionRecipe> {
		@Override
		public GemInsertionRecipe fromJson(ResourceLocation id, JsonObject jsonObject) {
			return new GemInsertionRecipe(id);
		}

		@Override
		public GemInsertionRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
			return new GemInsertionRecipe(id);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buf, GemInsertionRecipe recipe) {
		}
	}
}