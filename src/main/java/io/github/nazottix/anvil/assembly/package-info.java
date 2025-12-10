/**
 * パーツアセンブリシステム
 *
 * ツールパーツを組み合わせてツールを作成するためのシステムです。
 *
 * 主要クラス:
 * - {@link io.github.nazottix.anvil.assembly.ToolAssembler} - パーツ検証と組み立て
 * - {@link io.github.nazottix.anvil.assembly.ToolBuilder} - Fluent APIでのツール構築
 * - {@link io.github.nazottix.anvil.assembly.ToolStatCalculator} - ステータス計算
 * - {@link io.github.nazottix.anvil.assembly.CalculatedStats} - 計算結果の保持
 * - {@link io.github.nazottix.anvil.assembly.AssemblyResult} - 組み立て結果
 *
 * 使用例:
 * <pre>
 * // ToolBuilderを使用した簡単な方法
 * ItemStack pickaxe = ToolBuilder.create(ToolType.PICKAXE)
 *     .head("anvil:iron", Grade.A)
 *     .handle("anvil:oak", Grade.B)
 *     .binding("anvil:copper", Grade.C)
 *     .build();
 *
 * // ToolAssemblerを使用した詳細な方法
 * AssemblyResult result = ToolAssembler.assemble(
 *     ToolType.PICKAXE,
 *     List.of(
 *         new ToolPart("anvil:iron", "head", "a"),
 *         new ToolPart("anvil:oak", "handle", "b"),
 *         new ToolPart("anvil:copper", "binding", "c")
 *     )
 * );
 * </pre>
 *
 * @see io.github.nazottix.anvil.tool.ToolType
 * @see io.github.nazottix.anvil.tool.PartType
 * @see io.github.nazottix.anvil.material.Material
 */
@javax.annotation.ParametersAreNonnullByDefault
package io.github.nazottix.anvil.assembly;
