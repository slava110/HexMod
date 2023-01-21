package at.petrak.hexcasting.common.casting.operators.akashic

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.MishapNoAkashicRecord
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicRecord
import at.petrak.hexcasting.common.lib.HexSounds
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundSource

object OpAkashicWrite : SpellAction {
    override val argc = 3

    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val pos = args.getBlockPos(0, argc)
        val key = args.getPattern(1, argc)
        val datum = args.get(2)

        ctx.assertVecInRange(pos)

        val record = ctx.world.getBlockState(pos).block
        if (record !is BlockAkashicRecord) {
            throw MishapNoAkashicRecord(pos)
        }

        val trueName = MishapOthersName.getTrueNameFromDatum(datum, ctx.caster)
        if (trueName != null)
            throw MishapOthersName(trueName)

        return Triple(
            Spell(record, pos, key, datum),
            MediaConstants.DUST_UNIT,
            listOf()
        )
    }

    private data class Spell(
        val record: BlockAkashicRecord,
        val recordPos: BlockPos,
        val key: HexPattern,
        val datum: Iota
    ) :
        RenderedSpell {
        override fun cast(ctx: CastingContext) {
            record.addNewDatum(recordPos, ctx.world, key, datum)

            ctx.world.playSound(
                null, recordPos, HexSounds.SCROLL_SCRIBBLE, SoundSource.BLOCKS,
                1f, 0.8f
            )

            // val colorizer = HexPlayerDataHelper.getColorizer(ctx.caster)
            // val normal = record.blockState.getValue(BlockAkashicBookshelf.FACING).normal
            // ParticleSpray(
            //     Vec3.atCenterOf(record.blockPos), Vec3.atBottomCenterOf(normal),
            //     0.5, Math.PI / 4, 10
            // ).sprayParticles(ctx.world, colorizer)
        }
    }
}
